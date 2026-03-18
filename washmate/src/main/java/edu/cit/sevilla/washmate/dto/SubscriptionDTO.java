package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    private Long subscriptionId;
    private Long userId;
    private String planType;
    private BigDecimal planPrice;
    private Integer ordersIncluded;
    private Integer ordersUsed;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
}
