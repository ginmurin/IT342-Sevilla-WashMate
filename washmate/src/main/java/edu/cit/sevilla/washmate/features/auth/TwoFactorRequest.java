package edu.cit.sevilla.washmate.features.auth;

import lombok.Data;

@Data
public class TwoFactorRequest {
    private String code;
}

