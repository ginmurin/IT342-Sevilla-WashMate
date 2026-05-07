package edu.cit.sevilla.washmate.features.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscription;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import edu.cit.sevilla.washmate.features.subscriptions.SubscriptionService;

@Service
@Slf4j
public class GoogleOAuthService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionService subscriptionService;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    private final HttpTransport httpTransport = new NetHttpTransport();
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public GoogleOAuthService(UserRepository userRepository,
                             UserSubscriptionRepository userSubscriptionRepository,
                             SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Verify Google ID token and extract user information
     */
    public GoogleUserInfo verifyAndExtractGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                log.warn("Invalid Google ID token");
                return null;
            }

            GoogleIdToken.Payload payload = token.getPayload();

            return GoogleUserInfo.builder()
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .givenName((String) payload.get("given_name"))
                    .familyName((String) payload.get("family_name"))
                    .picture((String) payload.get("picture"))
                    .sub(payload.getSubject())
                    .emailVerified(payload.getEmailVerified())
                    .build();
        } catch (Exception ex) {
            log.error("Error verifying Google ID token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Process Google OAuth login - create or link user account
     */
    @Transactional
    public User processGoogleOAuth(GoogleUserInfo googleUser) {
        try {
            // Check if user exists by Google ID
            Optional<User> existingByGoogle = userRepository.findByOauthId(googleUser.getSub());
            if (existingByGoogle.isPresent()) {
                User user = existingByGoogle.get();
                // Update last login timestamp
                user = updateLastLogin(user);
                log.info("Google OAuth user found and updated: {}", googleUser.getEmail());
                return user;
            }

            // Check if user exists by email
            Optional<User> existingByEmail = userRepository.findByEmail(googleUser.getEmail());
            if (existingByEmail.isPresent()) {
                User user = existingByEmail.get();

                // If user already has a different OAuth provider, reject linking
                if (user.getOauthProvider() != null && !user.getOauthProvider().equals("GOOGLE")) {
                    log.warn("Email already linked to different provider: {}", googleUser.getEmail());
                    throw new RuntimeException("Email already linked to a different authentication provider");
                }

                // Link Google OAuth to existing email user
                user.setOauthId(googleUser.getSub());
                user.setOauthProvider("GOOGLE");
                user.setEmailVerified(true); // Google emails are pre-verified
                user = updateLastLogin(user);
                user = userRepository.save(user);
                log.info("Google OAuth linked to existing user: {}", googleUser.getEmail());
                return user;
            }

            // Create new user from Google info
            User newUser = User.builder()
                    .email(googleUser.getEmail())
                    .firstName(googleUser.getGivenName() != null ? googleUser.getGivenName() : "User")
                    .lastName(googleUser.getFamilyName() != null ? googleUser.getFamilyName() : "")
                    .oauthId(googleUser.getSub())
                    .oauthProvider("GOOGLE")
                    .emailVerified(true) // Google emails are pre-verified
                    .role("CUSTOMER")
                    .build();

            newUser = userRepository.save(newUser);
            initializeUserSubscription(newUser);

            log.info("New user created via Google OAuth: {}", googleUser.getEmail());
            return newUser;
        } catch (RuntimeException ex) {
            log.error("Error processing Google OAuth: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in Google OAuth processing: {}", ex.getMessage());
            throw new RuntimeException("Failed to process Google OAuth", ex);
        }
    }

    /**
     * Initialize subscription for new user
     */
    private void initializeUserSubscription(User user) {
        Optional<UserSubscription> existing = userSubscriptionRepository
                .findFirstByUserUserIdOrderByCreatedAtDesc(user.getUserId());

        if (existing.isEmpty()) {
            Subscription freePlan = subscriptionService.getOrCreateFreePlan();
            LocalDateTime now = LocalDateTime.now();

            UserSubscription subscription = UserSubscription.builder()
                    .user(user)
                    .subscription(freePlan)
                    .startDate(now)
                    .expiryDate(now.plusMonths(1))
                    .status("ACTIVE")
                    .build();
            userSubscriptionRepository.save(subscription);
        }
    }

    /**
     * Update user's last login timestamp
     */
    private User updateLastLogin(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * DTO for Google user information
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class GoogleUserInfo {
        private String email;
        private String name;
        private String givenName;
        private String familyName;
        private String picture;
        private String sub;
        private Boolean emailVerified;
    }
}

