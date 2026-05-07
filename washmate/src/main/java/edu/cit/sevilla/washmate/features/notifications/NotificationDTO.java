package edu.cit.sevilla.washmate.features.notifications;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long notificationId;
    private Long userId;
    private String notificationType;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private Boolean isRead;
    private Boolean isSent;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}

