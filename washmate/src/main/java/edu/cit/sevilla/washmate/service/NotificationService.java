package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.entity.Notification;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.repository.NotificationRepository;
import edu.cit.sevilla.washmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Get all notifications for a user (paginated, latest first)
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserUserIdAndIsReadFalse(userId);
    }

    /**
     * Get count of unread notifications
     */
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Create and send a notification
     */
    @Transactional
    public Notification createNotification(Long userId, String type, String title, String message,
                                           String referenceType, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .isSent(true)
                .sentAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Create order status update notification
     */
    public void notifyOrderStatusUpdate(Long userId, Long orderId, String orderNumber, String status) {
        String title = "Order " + orderNumber + " Updated";
        String message = "Your order status: " + status.replace("_", " ");
        createNotification(userId, "ORDER_UPDATE", title, message, "ORDER", orderId);
    }

    /**
     * Create payment notification
     */
    public void notifyPaymentSuccess(Long userId, Long orderId, String orderNumber) {
        String title = "Payment Confirmed";
        String message = "Payment received for order " + orderNumber;
        createNotification(userId, "PAYMENT", title, message, "PAYMENT", orderId);
    }

    /**
     * Create subscription notification
     */
    public void notifySubscriptionUpgrade(Long userId, String planType) {
        String title = "Subscription Upgraded";
        String message = "You've successfully upgraded to " + planType + " plan!";
        createNotification(userId, "PROMOTION", title, message, "SUBSCRIPTION", userId);
    }

    /**
     * Create wallet top-up notification
     */
    public void notifyWalletTopup(Long userId, java.math.BigDecimal amount) {
        String title = "Wallet Top-up Successful";
        String message = "Added ₱" + amount.setScale(2, java.math.RoundingMode.HALF_UP) + " to your wallet";
        createNotification(userId, "PAYMENT", title, message, "WALLET_TOPUP", userId);
    }
}
