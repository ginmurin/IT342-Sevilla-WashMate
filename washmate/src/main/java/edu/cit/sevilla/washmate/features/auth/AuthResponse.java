package edu.cit.sevilla.washmate.features.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private Boolean requiresEmailVerification;
    private Boolean requiresTwoFactor;
    private String message;

    // Legacy constructor for backward compatibility
    public AuthResponse(String token, Long userId, String username, String firstName, String lastName, String email, String role) {
        this.accessToken = token;
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
}


