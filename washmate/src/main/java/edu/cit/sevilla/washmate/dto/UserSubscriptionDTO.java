package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserSubscriptionDTO {
    private Long userSubscriptionId;
    private Long userId;
    private Long subscriptionId;
    private String planType;
    private BigDecimal planPrice;
    private Integer discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private String status;  // ACTIVE, CANCELLED, EXPIRED
    private LocalDateTime createdAt;
}
