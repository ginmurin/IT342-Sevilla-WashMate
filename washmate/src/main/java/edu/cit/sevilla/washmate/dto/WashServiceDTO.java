package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WashServiceDTO {
    private Long serviceId;
    private String serviceName;
    private BigDecimal basePricePerUnit;
    private String unitType;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
