package edu.cit.sevilla.washmate.features.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleIdTokenRequest {
    @NotBlank(message = "Google ID token is required")
    private String idToken;
}

