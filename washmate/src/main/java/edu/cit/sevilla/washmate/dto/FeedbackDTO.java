package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackDTO {
    private Long feedbackId;
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private Long shopId;
    private String shopName;
    private Integer starRating;
    private String feedbackType;
    private String commentText;
    private String adminResponse;
    private String status;
    private LocalDateTime createdAt;
}
