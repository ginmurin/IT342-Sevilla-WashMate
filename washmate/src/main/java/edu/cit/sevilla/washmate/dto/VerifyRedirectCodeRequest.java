package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyRedirectCodeRequest {
    @NotBlank(message = "Redirect code is required")
    private String redirectCode;
}
