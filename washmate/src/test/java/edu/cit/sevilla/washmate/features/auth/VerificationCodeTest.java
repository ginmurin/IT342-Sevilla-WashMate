package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class VerificationCodeTest {

    @Test
    void isValid_WhenNotUsedAndNotExpired() {
        VerificationCode code = VerificationCode.builder()
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(0)
                .build();
        assertTrue(code.isValid());
    }

    @Test
    void isValid_WhenUsed_ReturnsFalse() {
        VerificationCode code = VerificationCode.builder()
                .isUsed(true)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(0)
                .build();
        assertFalse(code.isValid());
    }

    @Test
    void isExpired_WhenPastExpiry() {
        VerificationCode code = VerificationCode.builder()
                .isUsed(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .failedAttempts(0)
                .build();
        assertTrue(code.isExpired());
    }

    @Test
    void isExpired_WhenNotPastExpiry() {
        VerificationCode code = VerificationCode.builder()
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(0)
                .build();
        assertFalse(code.isExpired());
    }

    @Test
    void isMaxAttemptsExceeded_WhenAtLimit() {
        VerificationCode code = VerificationCode.builder()
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(3)
                .build();
        assertTrue(code.isMaxAttemptsExceeded());
    }

    @Test
    void isMaxAttemptsExceeded_WhenBelowLimit() {
        VerificationCode code = VerificationCode.builder()
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(2)
                .build();
        assertFalse(code.isMaxAttemptsExceeded());
    }

    @Test
    void incrementFailedAttempts_IncrementsCorrectly() {
        VerificationCode code = VerificationCode.builder()
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(0)
                .build();
        code.incrementFailedAttempts();
        assertEquals(1, code.getFailedAttempts());

        code.incrementFailedAttempts();
        assertEquals(2, code.getFailedAttempts());
    }
}
