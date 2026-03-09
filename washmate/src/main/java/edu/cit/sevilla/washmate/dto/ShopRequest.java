package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopRequest {

    @NotBlank(message = "Shop name is required")
    private String shopName;

    private String shopContact;
    private String shopAddress;
    private String serviceArea;
}
