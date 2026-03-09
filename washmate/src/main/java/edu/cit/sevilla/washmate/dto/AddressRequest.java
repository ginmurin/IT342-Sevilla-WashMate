package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    private String label;

    @NotBlank(message = "Full address is required")
    private String fullAddress;

    private String city;
    private Double latitude;
    private Double longitude;
    private Boolean isDefault = false;
}
