package edu.cit.sevilla.washmate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send OTP verification email
     */
    public void sendVerificationEmail(String toEmail, String otp, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("WashMate - Email Verification");
            message.setText(buildVerificationEmailBody(username, otp));
            message.setFrom("noreply@washmate.app");

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send verification email", ex);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String resetCode, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("WashMate - Password Reset");
            message.setText(buildPasswordResetEmailBody(username, resetCode));
            message.setFrom("noreply@washmate.app");

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send password reset email", ex);
        }
    }

    /**
     * Send account created welcome email
     */
    public void sendAccountCreatedEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Welcome to WashMate!");
            message.setText(buildWelcomeEmailBody(username));
            message.setFrom("noreply@washmate.app");

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send welcome email", ex);
        }
    }

    /**
     * Build verification email body
     */
    private String buildVerificationEmailBody(String username, String otp) {
        return "Hello " + username + ",\n\n" +
                "Thank you for creating your WashMate account. Please verify your email address using the code below:\n\n" +
                "Verification Code: " + otp + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you did not create this account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The WashMate Team";
    }

    /**
     * Build password reset email body
     */
    private String buildPasswordResetEmailBody(String username, String resetCode) {
        return "Hello " + username + ",\n\n" +
                "We received a request to reset your password. Please use the code below to reset your password:\n\n" +
                "Reset Code: " + resetCode + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The WashMate Team";
    }

    /**
     * Build welcome email body
     */
    private String buildWelcomeEmailBody(String username) {
        return "Hello " + username + ",\n\n" +
                "Welcome to WashMate! Your account has been successfully created.\n\n" +
                "You can now log in and start using our laundry services.\n\n" +
                "If you have any questions or need assistance, please don't hesitate to contact us.\n\n" +
                "Best regards,\n" +
                "The WashMate Team";
    }
}
