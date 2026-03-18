package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShopDTO {
    private Long shopId;
    private Long ownerId;
    private String ownerName;
    private String shopName;
    private String shopContact;
    private String shopAddress;
    private String serviceArea;
    private Boolean isActive;
    private BigDecimal ratingAverage;
    private LocalDateTime createdAt;
}
