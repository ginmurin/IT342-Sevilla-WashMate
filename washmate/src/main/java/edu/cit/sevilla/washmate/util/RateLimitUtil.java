package edu.cit.sevilla.washmate.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RateLimitUtil {

    /**
     * Check if an action is within rate limit
     * Rate limiting disabled - always allows
     */
    public boolean isWithinLimit(String key, int maxAttempts, int windowMinutes) {
        return true;
    }

    /**
     * Increment attempt counter
     * Rate limiting disabled - no-op
     */
    public void incrementAttempt(String key, int windowMinutes) {
        // No-op
    }

    /**
     * Reset attempt counter
     * Rate limiting disabled - no-op
     */
    public void reset(String key) {
        // No-op
    }

    /**
     * Get remaining attempts
     * Rate limiting disabled - always return max
     */
    public int getRemainingAttempts(String key, int maxAttempts) {
        return maxAttempts;
    }

    /**
     * Check cooldown period
     * Rate limiting disabled - always return true (passed)
     */
    public boolean isCooldownPassed(String key, int cooldownSeconds) {
        return true;
    }

    /**
     * Set a value with expiration
     * Rate limiting disabled - no-op
     */
    public void setValue(String key, String value, int expirationMinutes) {
        // No-op
    }
}
