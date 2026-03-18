package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.AuthResponse;
import edu.cit.sevilla.washmate.dto.RegisterRequest;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public AuthResponse syncUser(RegisterRequest request, String oauthId, String tokenValue) {
        // Find existing user by oauthId or create a new one
        User user = userRepository.findByOauthId(oauthId).orElse(null);

        if (user == null) {
            // Check by email if the user was created before Supabase migration but not linked yet
            user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            if (user != null) {
                // Link the existing user with the Supabase UUID
                user.setOauthId(oauthId);
                user.setOauthProvider("SUPABASE");
                userRepository.save(user);
            } else {
                // Completely new user
                String role = (request.getRole() != null && !request.getRole().isBlank())
                        ? request.getRole().toUpperCase()
                        : "CUSTOMER";

                User newUser = User.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .oauthId(oauthId)
                        .oauthProvider("SUPABASE")
                        .phoneNumber(request.getPhoneNumber())
                        .role(role)
                        .emailVerified(true) // Supabase handles verification
                        .build();

                user = userRepository.save(newUser);
            }
        }

        return new AuthResponse(tokenValue, user.getUserId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
    }

    public Optional<String> findEmailByUsername(String username) {
        return userRepository.findByUsername(username).map(User::getEmail);
    }
}

