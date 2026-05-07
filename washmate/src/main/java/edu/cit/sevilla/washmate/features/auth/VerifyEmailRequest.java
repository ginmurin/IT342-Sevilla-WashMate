package edu.cit.sevilla.washmate.features.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import edu.cit.sevilla.washmate.features.users.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyEmailRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Verification code is required")
    private String code;
}

