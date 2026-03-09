package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.AuthResponse;
import edu.cit.sevilla.washmate.dto.LoginRequest;
import edu.cit.sevilla.washmate.dto.RegisterRequest;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String role = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().toUpperCase()
                : "CUSTOMER";

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, Map.of("role", user.getRole()));

        return new AuthResponse(token, user.getUserId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        // Resolve email from either an email address or a username
        String email = request.getEmailOrUsername().contains("@")
                ? request.getEmailOrUsername()
                : userRepository.findByUsername(request.getEmailOrUsername())
                        .map(User::getEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, Map.of("role", user.getRole()));

        return new AuthResponse(token, user.getUserId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
    }
}
