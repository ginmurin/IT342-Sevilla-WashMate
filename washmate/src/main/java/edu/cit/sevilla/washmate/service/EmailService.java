package edu.cit.sevilla.washmate.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String otp, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("WashMate - Email Verification");
            helper.setText(buildVerificationEmailBody(otp), true);
            helper.setFrom("noreply@washmate.app");
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (MessagingException ex) {
            log.error("Failed to send verification email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send verification email", ex);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetCode, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("WashMate - Password Reset");
            helper.setText(buildPasswordResetEmailBody(resetCode), true);
            helper.setFrom("noreply@washmate.app");
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException ex) {
            log.error("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send password reset email", ex);
        }
    }

    public void sendTwoFactorEmail(String toEmail, String otp, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("WashMate - Two-Factor Authentication Code");
            helper.setText(buildTwoFactorEmailBody(otp), true);
            helper.setFrom("noreply@washmate.app");
            mailSender.send(message);
            log.info("Two-factor code sent to: {}", toEmail);
        } catch (MessagingException ex) {
            log.error("Failed to send two-factor email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send two-factor email", ex);
        }
    }

    public void sendAccountCreatedEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to WashMate!");
            helper.setText(buildWelcomeEmailBody(username), true);
            helper.setFrom("noreply@washmate.app");
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (MessagingException ex) {
            log.error("Failed to send welcome email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send welcome email", ex);
        }
    }

    private String buildVerificationEmailBody(String otp) {
        return getHtmlTemplate("Email Verification", "Verify Your WashMate Account",
            "Thank you for creating your WashMate account. Please verify your email address using the code below:", otp);
    }

    private String buildPasswordResetEmailBody(String resetCode) {
        return getHtmlTemplate("Password Reset", "Reset Your Password",
            "We received a request to reset your password. Please use the code below to reset your password:", resetCode);
    }

    private String buildTwoFactorEmailBody(String otp) {
        return getHtmlTemplate("Two-Factor Authentication", "Enable Two-Factor Authentication",
            "Use the verification code below to enable two-factor authentication and secure your account:", otp);
    }

    private String buildWelcomeEmailBody(String username) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #0891b2 0%, #06b6d4 100%); color: white; padding: 40px 20px; border-radius: 8px 8px 0 0; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".content { background-color: #f8fafc; padding: 30px 20px; border-radius: 0 0 8px 8px; }" +
                ".features { background: white; border-radius: 6px; padding: 20px; margin: 20px 0; }" +
                ".feature { padding: 12px 0; border-bottom: 1px solid #e2e8f0; }" +
                ".feature:last-child { border-bottom: none; }" +
                ".feature span { color: #0891b2; font-weight: bold; margin-right: 8px; }" +
                ".security-note { background: #ecfdf5; border-left: 4px solid #10b981; padding: 12px; border-radius: 4px; margin: 20px 0; font-size: 14px; color: #047857; }" +
                ".footer { text-align: center; color: #64748b; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e2e8f0; }" +
                "</style></head><body>" +
                "<div class=\"container\">" +
                "<div class=\"header\"><h1>Welcome to WashMate!</h1></div>" +
                "<div class=\"content\">" +
                "<p>Hello <strong>" + username + "</strong>,</p>" +
                "<p>Your account has been successfully created! You are now ready to enjoy our premium laundry services.</p>" +
                "<div class=\"features\">" +
                "<div class=\"feature\"><span>+</span><strong>Easy Booking</strong> - Schedule pickups and dropoffs at your convenience</div>" +
                "<div class=\"feature\"><span>+</span><strong>Fast Service</strong> - Professional washing and ironing within 24 hours</div>" +
                "<div class=\"feature\"><span>+</span><strong>Flexible Pricing</strong> - Subscription plans and pay-as-you-go options</div>" +
                "<div class=\"feature\"><span>+</span><strong>Secure Wallet</strong> - Top up your balance for faster transactions</div>" +
                "</div>" +
                "<div class=\"security-note\">" +
                "<strong>Security Tip:</strong> We recommend enabling two-factor authentication in your account settings to keep your account secure." +
                "</div>" +
                "<p style=\"text-align: center; margin-top: 30px;\">" +
                "<a href=\"https://washmate.app/customer\" style=\"display: inline-block; background: linear-gradient(135deg, #0891b2 0%, #06b6d4 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: 600;\">Start Using WashMate</a>" +
                "</p>" +
                "<p style=\"font-size: 14px; color: #64748b; margin-top: 30px;\">" +
                "If you have any questions, contact our support team at <strong>support@washmate.app</strong>" +
                "</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>2024 WashMate. All rights reserved.</p>" +
                "<p>This is an automated email, please do not reply directly.</p>" +
                "</div></div></body></html>";
    }

    private String getHtmlTemplate(String title, String heading, String message, String code) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f8fafc; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".wrapper { background-color: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #0891b2 0%, #06b6d4 100%); color: white; padding: 40px 20px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; font-weight: 600; }" +
                ".header p { margin: 8px 0 0 0; font-size: 14px; opacity: 0.95; }" +
                ".content { padding: 40px 30px; }" +
                ".message { font-size: 16px; color: #334155; margin-bottom: 24px; line-height: 1.8; }" +
                ".code-section { background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%); border-left: 4px solid #0891b2; padding: 24px; border-radius: 6px; margin: 30px 0; text-align: center; }" +
                ".code-label { font-size: 12px; color: #0891b2; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }" +
                ".code { font-size: 32px; font-weight: bold; color: #0891b2; letter-spacing: 4px; font-family: Courier New, monospace; }" +
                ".expiry { font-size: 13px; color: #64748b; margin-top: 12px; }" +
                ".warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px; border-radius: 4px; margin: 20px 0; font-size: 14px; color: #d97706; }" +
                ".footer { text-align: center; color: #94a3b8; font-size: 12px; padding-top: 24px; border-top: 1px solid #e2e8f0; }" +
                ".footer p { margin: 4px 0; }" +
                "</style></head><body>" +
                "<div class=\"container\"><div class=\"wrapper\">" +
                "<div class=\"header\"><h1>" + heading + "</h1><p>Secure Your WashMate Account</p></div>" +
                "<div class=\"content\">" +
                "<div class=\"message\">Hello!<br><br>" + message + "</div>" +
                "<div class=\"code-section\">" +
                "<div class=\"code-label\">Your Verification Code</div>" +
                "<div class=\"code\">" + code + "</div>" +
                "<div class=\"expiry\">This code will expire in 10 minutes</div>" +
                "</div>" +
                "<p>Enter this code in the WashMate app or website to complete the " + title.toLowerCase() + ".</p>" +
                "<div class=\"warning\">" +
                "<strong>Security Alert:</strong> Never share this code with anyone. WashMate support will never ask for it." +
                "</div>" +
                "<p style=\"margin-top: 30px; padding-top: 20px; border-top: 1px solid #e2e8f0; font-size: 14px; color: #64748b;\">" +
                "<strong>Didn't request this?</strong><br>" +
                "If you didn't request a " + title.toLowerCase() + ", you can safely ignore this email. Your account remains secure." +
                "</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>2024 WashMate. All rights reserved.</p>" +
                "<p>Helping you keep your laundry clean and your account secure.</p>" +
                "</div></div></div></body></html>";
    }
}
