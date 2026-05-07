package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TwoFactorResendRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
}
