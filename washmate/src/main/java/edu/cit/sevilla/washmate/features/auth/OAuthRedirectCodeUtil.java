package edu.cit.sevilla.washmate.features.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Component;
import edu.cit.sevilla.washmate.features.users.User;

@Component
@Slf4j
public class OAuthRedirectCodeUtil {

    // Redis disabled - using in-memory storage for now
    // In production, re-enable Redis integration

    /**
     * Generate random state parameter for CSRF protection
     */
    public String generateState() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('A', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .build();
        return generator.generate(32);
    }

    /**
     * Store state parameter with expiration (5 minutes)
     * Disabled - using no-op without Redis
     */
    public void storeState(String state) {
        // No-op - Redis disabled
    }

    /**
     * Validate and consume state parameter
     * Disabled - using no-op without Redis
     */
    public boolean validateAndConsumeState(String state) {
        // Allow all for now - Redis disabled
        return true;
    }

    /**
     * Generate short-lived redirect code (8 characters, expires in 5 minutes)
     */
    public String generateRedirectCode() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .build();
        return generator.generate(8);
    }

    /**
     * Store redirect code with associated tokens and user data
     * Disabled - using no-op without Redis
     */
    public void storeRedirectCode(String code, String accessToken, String refreshToken, Long userId, String email, String role) {
        // No-op - Redis disabled
        log.debug("Redirect code generated (Redis disabled): {}", code);
    }

    /**
     * Retrieve and consume redirect code
     * Disabled - using no-op without Redis
     */
    public RedirectCodeData getAndConsumeRedirectCode(String code) {
        // Return null - Redis disabled
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RedirectCodeData {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String email;
        private String role;
    }
}

