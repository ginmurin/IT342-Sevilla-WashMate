package edu.cit.sevilla.washmate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceVariantDTO {
    private Long variantId;
    private String variantName;
    private BigDecimal variantPrice;
    private Integer displayOrder;
    private Boolean isActive;
}
