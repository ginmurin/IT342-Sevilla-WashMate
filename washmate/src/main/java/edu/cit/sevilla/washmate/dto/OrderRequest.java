package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderRequest {

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private Long pickupAddressId;
    private Long deliveryAddressId;

    private BigDecimal totalWeight;

    private String specialInstructions;

    private LocalDateTime pickupSchedule;
    private LocalDateTime deliverySchedule;

    private Boolean isRushOrder = false;
}
