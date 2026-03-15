package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    private Long subscriptionId;
    private String planType;
    private BigDecimal planPrice;
    private Integer ordersIncluded;
    private Integer discountPercentage;
    private LocalDateTime createdAt;
}
