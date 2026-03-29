package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    // Multiple services per order
    @NotEmpty(message = "At least one service is required")
    private List<OrderServiceInput> services;

    private Long pickupAddressId;
    private Long deliveryAddressId;

    // Address information from frontend (for creating new addresses)
    private String pickupAddressString;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    private String deliveryAddressString;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    private BigDecimal totalWeight;

    private String specialInstructions;

    private LocalDateTime pickupSchedule;
    private LocalDateTime deliverySchedule;

    private Boolean isRushOrder = false;

    // Delivery fee calculated by frontend (validated server-side)
    private BigDecimal deliveryFee = BigDecimal.ZERO;
}
