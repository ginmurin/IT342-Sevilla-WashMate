package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
}
