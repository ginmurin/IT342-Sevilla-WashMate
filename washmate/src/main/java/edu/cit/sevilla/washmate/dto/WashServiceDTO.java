package edu.cit.sevilla.washmate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WashServiceDTO {
    private Long serviceId;
    private String serviceName;
    private BigDecimal basePricePerUnit;
    private String unitType;
    private String description;
    private Boolean isActive;
    private Boolean hasVariants;
    private Boolean isAutoSelected;
    private List<ServiceVariantDTO> variants;
    private LocalDateTime createdAt;
}
