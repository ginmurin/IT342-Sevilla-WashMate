package edu.cit.sevilla.washmate.controller;

import edu.cit.sevilla.washmate.dto.AuthResponse;
import edu.cit.sevilla.washmate.dto.RegisterRequest;
import edu.cit.sevilla.washmate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Sync endpoint for Supabase users. Expects a valid Supabase JWT Bearer token.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            AuthResponse response = authService.syncUser(request, jwt.getSubject(), jwt.getTokenValue());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * GET /api/auth/email-by-username?username=xxx
     * Public endpoint — resolves a username to its email so the frontend
     * can pass the email to Supabase signInWithPassword.
     */
    @GetMapping("/email-by-username")
    public ResponseEntity<?> emailByUsername(@RequestParam("username") String username) {
        return authService.findEmailByUsername(username)
                .map(email -> ResponseEntity.ok(Map.of("email", email)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No account found with that username")));
    }
}

