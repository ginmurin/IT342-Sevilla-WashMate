package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendVerificationEmail_Success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@test.com", "123456", "testuser"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_Success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("test@test.com", "123456", "testuser"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendTwoFactorEmail_Success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendTwoFactorEmail("test@test.com", "123456", "testuser"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendAccountCreatedEmail_Success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendAccountCreatedEmail("test@test.com", "testuser"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_ThrowsException_WhenMailFails() throws Exception {
        jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new jakarta.mail.MessagingException("Mail error")).when(mimeMessage).setFrom(any(jakarta.mail.Address.class));

        assertThrows(RuntimeException.class, () ->
                emailService.sendVerificationEmail("test@test.com", "123456", "testuser"));
    }

    @Test
    void sendPasswordResetEmail_ThrowsException_WhenMailFails() throws Exception {
        jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new jakarta.mail.MessagingException("Mail error")).when(mimeMessage).setFrom(any(jakarta.mail.Address.class));

        assertThrows(RuntimeException.class, () ->
                emailService.sendPasswordResetEmail("test@test.com", "123456", "testuser"));
    }

    @Test
    void sendTwoFactorEmail_ThrowsException_WhenMailFails() throws Exception {
        jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new jakarta.mail.MessagingException("Mail error")).when(mimeMessage).setFrom(any(jakarta.mail.Address.class));

        assertThrows(RuntimeException.class, () ->
                emailService.sendTwoFactorEmail("test@test.com", "123456", "testuser"));
    }

    @Test
    void sendAccountCreatedEmail_ThrowsException_WhenMailFails() throws Exception {
        jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new jakarta.mail.MessagingException("Mail error")).when(mimeMessage).setFrom(any(jakarta.mail.Address.class));

        assertThrows(RuntimeException.class, () ->
                emailService.sendAccountCreatedEmail("test@test.com", "testuser"));
    }
}
