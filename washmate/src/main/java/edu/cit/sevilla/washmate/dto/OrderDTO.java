package edu.cit.sevilla.washmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    // Multiple services per order
    private List<OrderServiceDTO> services;
    private Long pickupAddressId;
    private Long deliveryAddressId;
    private BigDecimal totalWeight;
    private BigDecimal totalAmount;
    private String status;
    private String specialInstructions;
    private LocalDateTime pickupSchedule;
    private LocalDateTime deliverySchedule;
    private Boolean isRushOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderServiceDTO {
        private Long orderServiceId;
        private Long serviceId;
        private String serviceName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
