package edu.cit.sevilla.washmate.features.users;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    @Pattern(regexp = "^(\\+?[0-9]{10,})?$", message = "Invalid phone number")
    private String phoneNumber;
}

