package edu.cit.sevilla.washmate.features.notifications;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        notification = Notification.builder()
                .notificationId(1L)
                .user(user)
                .title("Test Title")
                .message("Test Message")
                .isRead(false)
                .build();
    }

    @Test
    void getUserNotifications_Success() {
        when(notificationRepository.findByUserUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(notification));

        List<Notification> result = notificationService.getUserNotifications(1L);

        assertEquals(1, result.size());
        assertEquals("Test Title", result.get(0).getTitle());
    }

    @Test
    void getUnreadNotifications_Success() {
        when(notificationRepository.findByUserUserIdAndIsReadFalse(1L))
                .thenReturn(Arrays.asList(notification));

        List<Notification> result = notificationService.getUnreadNotifications(1L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
    }

    @Test
    void getUnreadNotificationCount_Success() {
        when(notificationRepository.countByUserUserIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadNotificationCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.markAsRead(1L);

        assertTrue(result.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_ThrowsException_WhenNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(1L);
        });
    }

    @Test
    void markAllAsRead_Success() {
        Notification unread1 = Notification.builder().isRead(false).build();
        Notification unread2 = Notification.builder().isRead(false).build();
        
        when(notificationRepository.findByUserUserIdAndIsReadFalse(1L))
                .thenReturn(Arrays.asList(unread1, unread2));

        notificationService.markAllAsRead(1L);

        assertTrue(unread1.getIsRead());
        assertTrue(unread2.getIsRead());
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void createNotification_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.createNotification(
                1L, "INFO", "Title", "Message", "ORDER", 1L
        );

        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotification(
                    1L, "INFO", "Title", "Message", "ORDER", 1L
            );
        });
    }

    @Test
    void deleteNotification_Success() {
        doNothing().when(notificationRepository).deleteById(1L);

        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void notifyOrderStatusUpdate_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.notifyOrderStatusUpdate(1L, 1L, "ORD-123", "COMPLETED");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notifyPaymentSuccess_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.notifyPaymentSuccess(1L, 1L, "ORD-123");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notifySubscriptionUpgrade_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.notifySubscriptionUpgrade(1L, "PREMIUM");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notifyWalletTopup_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.notifyWalletTopup(1L, new BigDecimal("100.00"));

        verify(notificationRepository).save(any(Notification.class));
    }
}
