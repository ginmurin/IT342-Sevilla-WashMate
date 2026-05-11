package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

        @Mock
        private VerificationCodeRepository verificationCodeRepository;

        @Mock
        private EmailService emailService;

        @InjectMocks
        private VerificationCodeService verificationCodeService;

        private VerificationCode verificationCode;

        @BeforeEach
        void setUp() {
                verificationCode = VerificationCode.builder()
                                .id(1L)
                                .userId(1L)
                                .code("123456")
                                .codeType("EMAIL_VERIFICATION")
                                .expiresAt(LocalDateTime.now().plusMinutes(10))
                                .isUsed(false)
                                .failedAttempts(0)
                                .build();
        }

        @Test
        void generateAndSendCode_EmailVerification() {
                when(verificationCodeRepository.findByUserIdAndCodeType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.empty());
                when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

                assertDoesNotThrow(() -> verificationCodeService.generateAndSendCode(1L, "EMAIL_VERIFICATION",
                                "test@test.com", "testuser"));

                verify(emailService).sendVerificationEmail(eq("test@test.com"), anyString(), eq("testuser"));
        }

        @Test
        void generateAndSendCode_PasswordReset() {
                when(verificationCodeRepository.findByUserIdAndCodeType(1L, "PASSWORD_RESET"))
                                .thenReturn(Optional.empty());
                when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

                assertDoesNotThrow(() -> verificationCodeService.generateAndSendCode(1L, "PASSWORD_RESET",
                                "test@test.com", "testuser"));

                verify(emailService).sendPasswordResetEmail(eq("test@test.com"), anyString(), eq("testuser"));
        }

        @Test
        void generateAndSendCode_TwoFactorAuth() {
                when(verificationCodeRepository.findByUserIdAndCodeType(1L, "TWO_FACTOR_AUTH"))
                                .thenReturn(Optional.empty());
                when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

                assertDoesNotThrow(() -> verificationCodeService.generateAndSendCode(1L, "TWO_FACTOR_AUTH",
                                "test@test.com", "testuser"));

                verify(emailService).sendTwoFactorEmail(eq("test@test.com"), anyString(), eq("testuser"));
        }

        @Test
        void generateAndSendCode_TwoFactorLogin() {
                when(verificationCodeRepository.findByUserIdAndCodeType(1L, "TWO_FACTOR_LOGIN"))
                                .thenReturn(Optional.empty());
                when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

                assertDoesNotThrow(() -> verificationCodeService.generateAndSendCode(1L, "TWO_FACTOR_LOGIN",
                                "test@test.com", "testuser"));

                verify(emailService).sendTwoFactorEmail(eq("test@test.com"), anyString(), eq("testuser"));
        }

        @Test
        void generateAndSendCode_DeletesExistingCode() {
                VerificationCode existing = VerificationCode.builder().id(2L).userId(1L).build();
                when(verificationCodeRepository.findByUserIdAndCodeType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.of(existing));
                when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

                assertDoesNotThrow(() -> verificationCodeService.generateAndSendCode(1L, "EMAIL_VERIFICATION",
                                "test@test.com", "testuser"));

                verify(verificationCodeRepository).delete(existing);
        }

        @Test
        void verifyCode_Success() {
                when(verificationCodeRepository.findByUserIdAndCode(1L, "123456"))
                                .thenReturn(Optional.of(verificationCode));

                boolean result = verificationCodeService.verifyCode(1L, "123456", "EMAIL_VERIFICATION");

                assertTrue(result);
                assertTrue(verificationCode.getIsUsed());
                verify(verificationCodeRepository).save(verificationCode);
        }

        @Test
        void verifyCode_NotFound_ReturnsFalse() {
                when(verificationCodeRepository.findByUserIdAndCode(1L, "wrong"))
                                .thenReturn(Optional.empty());

                boolean result = verificationCodeService.verifyCode(1L, "wrong", "EMAIL_VERIFICATION");

                assertFalse(result);
        }

        @Test
        void verifyCode_WrongCodeType_ReturnsFalse() {
                when(verificationCodeRepository.findByUserIdAndCode(1L, "123456"))
                                .thenReturn(Optional.of(verificationCode));

                boolean result = verificationCodeService.verifyCode(1L, "123456", "PASSWORD_RESET");

                assertFalse(result);
        }

        @Test
        void verifyCode_AlreadyUsed_ReturnsFalse() {
                verificationCode.setIsUsed(true);
                when(verificationCodeRepository.findByUserIdAndCode(1L, "123456"))
                                .thenReturn(Optional.of(verificationCode));

                boolean result = verificationCodeService.verifyCode(1L, "123456", "EMAIL_VERIFICATION");

                assertFalse(result);
        }

        @Test
        void verifyCode_Expired_ReturnsFalse() {
                verificationCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));
                when(verificationCodeRepository.findByUserIdAndCode(1L, "123456"))
                                .thenReturn(Optional.of(verificationCode));

                boolean result = verificationCodeService.verifyCode(1L, "123456", "EMAIL_VERIFICATION");

                assertFalse(result);
                verify(verificationCodeRepository).delete(verificationCode);
        }

        @Test
        void verifyCode_MaxAttemptsExceeded_ReturnsFalse() {
                verificationCode.setFailedAttempts(3);
                when(verificationCodeRepository.findByUserIdAndCode(1L, "123456"))
                                .thenReturn(Optional.of(verificationCode));

                boolean result = verificationCodeService.verifyCode(1L, "123456", "EMAIL_VERIFICATION");

                assertFalse(result);
                verify(verificationCodeRepository).delete(verificationCode);
        }

        @Test
        void recordFailedAttempt_Increments() {
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.of(verificationCode));

                verificationCodeService.recordFailedAttempt(1L, "EMAIL_VERIFICATION");

                assertEquals(1, verificationCode.getFailedAttempts());
                verify(verificationCodeRepository).save(verificationCode);
        }

        @Test
        void recordFailedAttempt_DeletesWhenMaxExceeded() {
                verificationCode.setFailedAttempts(2); // Will become 3 after increment
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.of(verificationCode));

                verificationCodeService.recordFailedAttempt(1L, "EMAIL_VERIFICATION");

                verify(verificationCodeRepository).delete(verificationCode);
        }

        @Test
        void recordFailedAttempt_NoCodeFound() {
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.empty());

                assertDoesNotThrow(() -> verificationCodeService.recordFailedAttempt(1L, "EMAIL_VERIFICATION"));
        }

        @Test
        void resendCode_Success_NoExistingCode() {
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.empty());
                when(verificationCodeRepository.findByUserIdAndCodeType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.empty());
                when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(verificationCode);

                assertDoesNotThrow(() -> verificationCodeService.resendCode(1L, "EMAIL_VERIFICATION", "test@test.com",
                                "testuser"));
        }

        @Test
        void resendCode_TooSoon_ThrowsException() {
                VerificationCode recentCode = VerificationCode.builder()
                                .id(2L)
                                .userId(1L)
                                .codeType("EMAIL_VERIFICATION")
                                .createdAt(LocalDateTime.now()) // Just created
                                .expiresAt(LocalDateTime.now().plusMinutes(10))
                                .isUsed(false)
                                .failedAttempts(0)
                                .build();
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.of(recentCode));

                assertThrows(RuntimeException.class, () -> verificationCodeService.resendCode(1L, "EMAIL_VERIFICATION",
                                "test@test.com", "testuser"));
        }

        @Test
        void getRemainingAttempts_WithActiveCode() {
                verificationCode.setFailedAttempts(1);
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.of(verificationCode));

                int remaining = verificationCodeService.getRemainingAttempts(1L, "EMAIL_VERIFICATION");

                assertEquals(2, remaining);
        }

        @Test
        void getRemainingAttempts_NoActiveCode() {
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenReturn(Optional.empty());

                int remaining = verificationCodeService.getRemainingAttempts(1L, "EMAIL_VERIFICATION");

                assertEquals(3, remaining); // MAX_FAILED_ATTEMPTS
        }

        // ===== Exception Coverage Tests =====

        @Test
        void generateAndSendCode_ThrowsException() {
                when(verificationCodeRepository.findByUserIdAndCodeType(1L, "EMAIL_VERIFICATION"))
                                .thenThrow(new RuntimeException("Database error"));

                assertThrows(RuntimeException.class, () -> verificationCodeService.generateAndSendCode(1L,
                                "EMAIL_VERIFICATION", "test@test.com", "testuser"));
        }

        @Test
        void verifyCode_ThrowsException_ReturnsFalse() {
                when(verificationCodeRepository.findByUserIdAndCode(1L, "123456"))
                                .thenThrow(new RuntimeException("Database error"));

                boolean result = verificationCodeService.verifyCode(1L, "123456", "EMAIL_VERIFICATION");

                assertFalse(result);
        }

        @Test
        void recordFailedAttempt_ThrowsException_Logs() {
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenThrow(new RuntimeException("Database error"));

                // Should not throw exception, just log
                assertDoesNotThrow(() -> verificationCodeService.recordFailedAttempt(1L, "EMAIL_VERIFICATION"));
        }

        @Test
        void resendCode_ThrowsException() {
                when(verificationCodeRepository.findActiveByUserIdAndType(1L, "EMAIL_VERIFICATION"))
                                .thenThrow(new RuntimeException("Database error"));

                assertThrows(RuntimeException.class, () -> verificationCodeService.resendCode(1L, "EMAIL_VERIFICATION",
                                "test@test.com", "testuser"));
        }

        @Test
        void cleanupExpiredCodes_Success() {
                VerificationCode code1 = VerificationCode.builder().build();
                VerificationCode code2 = VerificationCode.builder().build();
                when(verificationCodeRepository.findExpiredOrUsedCodes(any(LocalDateTime.class)))
                                .thenReturn(java.util.List.of(code1, code2));

                verificationCodeService.cleanupExpiredCodes();

                verify(verificationCodeRepository).delete(code1);
                verify(verificationCodeRepository).delete(code2);
        }

        @Test
        void cleanupExpiredCodes_ThrowsException_Logs() {
                when(verificationCodeRepository.findExpiredOrUsedCodes(any(LocalDateTime.class)))
                                .thenThrow(new RuntimeException("Database error"));

                // Should not throw exception, just log
                assertDoesNotThrow(() -> verificationCodeService.cleanupExpiredCodes());
        }
}
