package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderServiceInput {
    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Quantity is required")
    @DecimalMin("0.01")
    private BigDecimal quantity;

    // For services with variants (e.g., Dry Clean), store the selected variant ID
    private Long selectedVariantId;
}
