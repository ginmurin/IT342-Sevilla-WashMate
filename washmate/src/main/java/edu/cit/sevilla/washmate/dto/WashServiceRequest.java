package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WashServiceRequest {

    @NotBlank(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal basePricePerUnit;

    @NotBlank(message = "Unit type is required")
    private String unitType;

    private String description;
}
