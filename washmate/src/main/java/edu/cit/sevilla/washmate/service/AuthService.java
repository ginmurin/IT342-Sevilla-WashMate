package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.AuthResponse;
import edu.cit.sevilla.washmate.dto.RegisterRequest;
import edu.cit.sevilla.washmate.entity.Subscription;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.UserSubscription;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionService subscriptionService;

    public AuthResponse syncUser(RegisterRequest request, String oauthId, String tokenValue) {
        // Find existing user by oauthId or create a new one
        User user = userRepository.findByOauthId(oauthId).orElse(null);

        if (user == null) {
            // Check by email if the user was created before Supabase migration but not linked yet
            user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            if (user != null) {
                // Link the existing user with the Supabase UUID
                user.setOauthId(oauthId);
                user.setOauthProvider("SUPABASE");
                userRepository.save(user);
                // Initialize UserSubscription if not exists
                initializeUserSubscription(user);
            } else {
                // Completely new user — assign FREE plan
                String role = (request.getRole() != null && !request.getRole().isBlank())
                        ? request.getRole().toUpperCase()
                        : "CUSTOMER";

                User newUser = User.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .oauthId(oauthId)
                        .oauthProvider("SUPABASE")
                        .phoneNumber(request.getPhoneNumber())
                        .role(role)
                        .emailVerified(true)
                        .build();

                user = userRepository.save(newUser);
                // Initialize UserSubscription for new user
                initializeUserSubscription(user);
            }
        }

        return new AuthResponse(tokenValue, user.getUserId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
    }

    /**
     * Initialize UserSubscription for a user (for new users or linking existing users).
     * Creates a FREE plan subscription if one doesn't exist.
     */
    private void initializeUserSubscription(User user) {
        // Check if user already has a UserSubscription
        Optional<UserSubscription> existing = userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(user.getUserId());

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

    public Optional<String> findEmailByUsername(String username) {
        return userRepository.findByUsername(username).map(User::getEmail);
    }
}

