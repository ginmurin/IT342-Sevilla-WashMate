package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private Long shopId;
    private String shopName;
    private Long serviceId;
    private String serviceName;
    private Long pickupAddressId;
    private Long deliveryAddressId;
    private BigDecimal totalWeight;
    private BigDecimal totalAmount;
    private String status;
    private String specialInstructions;
    private LocalDateTime pickupSchedule;
    private LocalDateTime deliverySchedule;
    private Boolean isRushOrder;
    private BigDecimal rushFee;
    private LocalDateTime createdAt;
}
