package edu.cit.sevilla.washmate.features.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import edu.cit.sevilla.washmate.features.users.User;

@Data
public class TwoFactorResendRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
}

