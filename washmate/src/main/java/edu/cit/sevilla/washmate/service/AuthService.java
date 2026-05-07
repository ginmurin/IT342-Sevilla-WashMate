package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.AuthResponse;
import edu.cit.sevilla.washmate.dto.RegisterRequest;
import edu.cit.sevilla.washmate.dto.UserSubscriptionDTO;
import edu.cit.sevilla.washmate.entity.Subscription;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.UserSubscription;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.repository.UserSubscriptionRepository;
import edu.cit.sevilla.washmate.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeService verificationCodeService;

    public AuthResponse syncUser(RegisterRequest request, String oauthId, String tokenValue) {
        // Find existing user by oauthId (if provided) or by email
        User user = null;

        if (oauthId != null) {
            user = userRepository.findByOauthId(oauthId).orElse(null);
        }

        if (user == null) {
            // Check by email if the user was created before or already exists
            user = userRepository.findByEmail(request.getEmail()).orElse(null);

            if (user == null) {
                // Completely new user — assign FREE plan
                String role = (request.getRole() != null && !request.getRole().isBlank())
                        ? request.getRole().toUpperCase()
                        : "CUSTOMER";

                User newUser = User.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .role(role)
                        .emailVerified(false)  // Email verification required
                        .build();

                user = userRepository.save(newUser);
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

    /**
     * Register user with email and password
     */
    public User registerWithEmailPassword(String email, String username, String firstName, String lastName, String password, String phoneNumber) {
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Check if username already exists
        if (username != null && userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        // Validate password strength (at least 8 chars, 1 uppercase, 1 number)
        if (!isPasswordStrong(password)) {
            throw new RuntimeException("Password must be at least 8 characters with at least one uppercase letter and one number");
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(password);

        // Create user
        User user = User.builder()
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordHash)
                .phoneNumber(phoneNumber)
                .role("CUSTOMER")
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        initializeUserSubscription(user);

        log.info("User registered successfully: {}", email);
        return user;
    }

    /**
     * Validate email and password login
     */
    public User validateEmailPassword(String emailOrUsername, String password) {
        // Find user by email or username
        User user = userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername))
                .orElse(null);

        if (user == null) {
            log.warn("Login attempt for non-existent user: {}", emailOrUsername);
            return null;
        }

        // Check if user has password set (not OAuth-only)
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            log.warn("User has no password set (OAuth-only): {}", emailOrUsername);
            return null;
        }

        // Validate password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", emailOrUsername);
            return null;
        }

        return user;
    }

    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole());
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        return jwtTokenProvider.generateRefreshToken(user.getUserId());
    }

    /**
     * Reset password using reset code
     */
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!isPasswordStrong(newPassword)) {
            throw new RuntimeException("Password must be at least 8 characters with at least one uppercase letter and one number");
        }

        String passwordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(passwordHash);

        userRepository.save(user);
        log.info("Password reset successfully for user: {}", email);
    }

    /**
     * Change password for authenticated user
     */
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new RuntimeException("Password not set for this account");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!isPasswordStrong(newPassword)) {
            throw new RuntimeException("Password must be at least 8 characters with at least one uppercase letter and one number");
        }

        String passwordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    /**
     * Get current subscription info for a user.
     */
    public UserSubscriptionDTO getUserSubscriptionInfo(Long userId) {
        return subscriptionService.getCurrentSubscription(userId)
                .map(subscriptionService::toUserSubscriptionDTO)
                .orElse(null);
    }

    /**
     * Check if password is strong enough
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");

        return hasUppercase && hasNumber;
    }

    /**
     * Mark email as verified
     */
    public void markEmailAsVerified(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email marked as verified for user: {}", userId);
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Alias for getUserById - used in controllers
     */
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Update user information
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Get authentication service instance
     */
    public AuthService getAuthService() {
        return this;
    }

    /**
     * Get verification code service instance
     */
    public VerificationCodeService getVerificationCodeService() {
        return verificationCodeService;
    }
}
