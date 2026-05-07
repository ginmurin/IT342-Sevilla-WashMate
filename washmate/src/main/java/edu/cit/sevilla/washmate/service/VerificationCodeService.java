package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.entity.VerificationCode;
import edu.cit.sevilla.washmate.repository.VerificationCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class VerificationCodeService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    /**
     * Generate OTP code and save to database, then send via email
     */
    @Transactional
    public void generateAndSendCode(Long userId, String codeType, String userEmail, String username) {
        try {
            // Delete existing code if any (to avoid duplicates)
            verificationCodeRepository.findByUserIdAndCodeType(userId, codeType)
                    .ifPresent(verificationCodeRepository::delete);

            // Generate 6-digit OTP
            String code = generateOTP();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

            // Create and save verification code
            VerificationCode verificationCode = VerificationCode.builder()
                    .userId(userId)
                    .code(code)
                    .codeType(codeType)
                    .expiresAt(expiresAt)
                    .isUsed(false)
                    .failedAttempts(0)
                    .build();

            verificationCodeRepository.save(verificationCode);

            // Send email based on code type
            if ("EMAIL_VERIFICATION".equals(codeType)) {
                emailService.sendVerificationEmail(userEmail, code, username);
            } else if ("PASSWORD_RESET".equals(codeType)) {
                emailService.sendPasswordResetEmail(userEmail, code, username);
            } else if ("TWO_FACTOR_AUTH".equals(codeType)) {
                emailService.sendTwoFactorEmail(userEmail, code, username);
            } else if ("TWO_FACTOR_LOGIN".equals(codeType)) {
                emailService.sendTwoFactorEmail(userEmail, code, username);
            }

            log.info("Verification code generated and sent for user {} (type: {})", userId, codeType);
        } catch (Exception ex) {
            log.error("Error generating and sending verification code: {}", ex.getMessage());
            throw new RuntimeException("Failed to generate verification code", ex);
        }
    }

    /**
     * Verify OTP code
     * @return true if code is valid and verified, false otherwise
     */
    @Transactional
    public boolean verifyCode(Long userId, String code, String codeType) {
        try {
            // Find code for user
            Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository.findByUserIdAndCode(userId, code);

            if (verificationCodeOpt.isEmpty()) {
                log.warn("Verification code not found for user {} (type: {})", userId, codeType);
                return false;
            }

            VerificationCode verificationCode = verificationCodeOpt.get();

            // Check if it's the correct type
            if (!codeType.equals(verificationCode.getCodeType())) {
                log.warn("Code type mismatch for user {}", userId);
                return false;
            }

            // Check if already used
            if (verificationCode.getIsUsed()) {
                log.warn("Verification code already used for user {}", userId);
                return false;
            }

            // Check if expired
            if (verificationCode.isExpired()) {
                log.warn("Verification code expired for user {}", userId);
                verificationCodeRepository.delete(verificationCode);
                return false;
            }

            // Check if max attempts exceeded
            if (verificationCode.isMaxAttemptsExceeded()) {
                log.warn("Max verification attempts exceeded for user {}", userId);
                verificationCodeRepository.delete(verificationCode);
                return false;
            }

            // Mark as used
            verificationCode.setIsUsed(true);
            verificationCodeRepository.save(verificationCode);

            log.info("Verification code verified successfully for user {} (type: {})", userId, codeType);
            return true;
        } catch (Exception ex) {
            log.error("Error verifying code: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Handle failed verification attempt
     */
    @Transactional
    public void recordFailedAttempt(Long userId, String codeType) {
        try {
            Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository.findActiveByUserIdAndType(userId, codeType);

            if (verificationCodeOpt.isPresent()) {
                VerificationCode verificationCode = verificationCodeOpt.get();
                verificationCode.incrementFailedAttempts();

                // If max attempts exceeded, delete the code
                if (verificationCode.isMaxAttemptsExceeded()) {
                    verificationCodeRepository.delete(verificationCode);
                    log.warn("Verification code deleted due to max attempts exceeded for user {}", userId);
                } else {
                    verificationCodeRepository.save(verificationCode);
                }
            }
        } catch (Exception ex) {
            log.error("Error recording failed attempt: {}", ex.getMessage());
        }
    }

    /**
     * Resend code with cooldown check
     */
    @Transactional
    public void resendCode(Long userId, String codeType, String userEmail, String username) {
        try {
            // Check if a valid code already exists (don't resend too quickly)
            Optional<VerificationCode> existingCode = verificationCodeRepository.findActiveByUserIdAndType(userId, codeType);

            if (existingCode.isPresent()) {
                VerificationCode existing = existingCode.get();
                LocalDateTime createdTime = existing.getCreatedAt();
                LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

                if (createdTime.isAfter(oneMinuteAgo)) {
                    log.warn("Resend requested too soon for user {}", userId);
                    throw new RuntimeException("Please wait before requesting a new code");
                }

                // Delete old code
                verificationCodeRepository.delete(existing);
            }

            // Generate and send new code
            generateAndSendCode(userId, codeType, userEmail, username);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error resending code: {}", ex.getMessage());
            throw new RuntimeException("Failed to resend code", ex);
        }
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateOTP() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', '9')
                .build();
        return generator.generate(OTP_LENGTH);
    }

    /**
     * Scheduled task to delete expired codes (runs daily at 2 AM)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredCodes() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);
            verificationCodeRepository.findExpiredOrUsedCodes(cutoffTime).forEach(verificationCodeRepository::delete);
            log.info("Cleanup task completed - removed expired verification codes");
        } catch (Exception ex) {
            log.error("Error during verification code cleanup: {}", ex.getMessage());
        }
    }

    /**
     * Get remaining attempts for a code
     */
    public int getRemainingAttempts(Long userId, String codeType) {
        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository.findActiveByUserIdAndType(userId, codeType);
        if (verificationCodeOpt.isPresent()) {
            return Math.max(0, MAX_FAILED_ATTEMPTS - verificationCodeOpt.get().getFailedAttempts());
        }
        return MAX_FAILED_ATTEMPTS;
    }
}
