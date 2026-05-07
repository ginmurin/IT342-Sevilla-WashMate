package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TwoFactorLoginRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Code is required")
    private String code;
}
