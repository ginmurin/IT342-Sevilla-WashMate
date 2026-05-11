package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitUtilTest {

    private RateLimitUtil rateLimitUtil;

    @BeforeEach
    void setUp() {
        rateLimitUtil = new RateLimitUtil();
    }

    @Test
    void isWithinLimit_AlwaysReturnsTrue() {
        assertTrue(rateLimitUtil.isWithinLimit("test-key", 5, 1));
    }

    @Test
    void incrementAttempt_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            rateLimitUtil.incrementAttempt("test-key", 1);
        });
    }

    @Test
    void reset_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            rateLimitUtil.reset("test-key");
        });
    }

    @Test
    void getRemainingAttempts_ReturnsMaxAttempts() {
        int remaining = rateLimitUtil.getRemainingAttempts("test-key", 5);
        assertEquals(5, remaining);
    }

    @Test
    void isCooldownPassed_AlwaysReturnsTrue() {
        assertTrue(rateLimitUtil.isCooldownPassed("test-key", 60));
    }

    @Test
    void setValue_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            rateLimitUtil.setValue("test-key", "value", 5);
        });
    }
}
