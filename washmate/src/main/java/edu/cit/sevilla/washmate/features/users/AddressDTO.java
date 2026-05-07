package edu.cit.sevilla.washmate.features.users;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AddressDTO {
    private Long addressId;
    private Long userId;
    private String label;
    private String fullAddress;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}

