package edu.cit.sevilla.washmate.facade;

import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Facade for JWT authentication and user lookup.
 * Centralizes repeated user extraction logic across controllers.
 *
 * Eliminates ~50 lines of duplicate code by consolidating:
 * - JWT subject extraction
 * - OAuth user lookup
 * - Error handling for missing users
 *
 * Usage: Inject this component in controllers instead of UserRepository directly.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

    private final UserRepository userRepository;

    /**
     * Extract OAuth ID from JWT and retrieve the associated User.
     * @param jwt The JWT token from Spring Security context
     * @return The authenticated User
     * @throws RuntimeException if user not found
     */
    public User getAuthenticatedUser(Jwt jwt) {
        String oauthId = jwt.getSubject();
        return userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new RuntimeException("User not found: " + oauthId));
    }

    /**
     * Extract OAuth ID from JWT without loading user.
     * @param jwt The JWT token from Spring Security context
     * @return The OAuth ID
     */
    public String getOAuthId(Jwt jwt) {
        return jwt.getSubject();
    }
}
