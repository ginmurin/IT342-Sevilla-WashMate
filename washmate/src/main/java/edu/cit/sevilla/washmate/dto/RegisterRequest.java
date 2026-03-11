package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Password not required — Supabase owns authentication
    private String password;

    private String phoneNumber;

    /** CUSTOMER (default) or SHOP_OWNER */
    private String role;
}
