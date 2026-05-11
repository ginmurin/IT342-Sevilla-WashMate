package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;

import edu.cit.sevilla.washmate.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("super-secret-key-for-testing-must-be-at-least-32-chars-long!!");
        jwtConfig.setAccessTokenExpiration(900000L);  // 15 min
        jwtConfig.setRefreshTokenExpiration(604800000L); // 7 days
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
    }

    @Test
    void generateAccessToken_Success() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@test.com", "CUSTOMER");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void generateRefreshToken_Success() {
        String token = jwtTokenProvider.generateRefreshToken(1L);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void validateToken_ValidToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@test.com", "CUSTOMER");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    void extractUserId_Success() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@test.com", "CUSTOMER");
        Long userId = jwtTokenProvider.extractUserId(token);
        assertEquals(1L, userId);
    }

    @Test
    void extractUserId_InvalidToken_ReturnsNull() {
        Long userId = jwtTokenProvider.extractUserId("invalid-token");
        assertNull(userId);
    }

    @Test
    void extractEmail_Success() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@test.com", "CUSTOMER");
        String email = jwtTokenProvider.extractEmail(token);
        assertEquals("test@test.com", email);
    }

    @Test
    void extractEmail_InvalidToken_ReturnsNull() {
        String email = jwtTokenProvider.extractEmail("invalid-token");
        assertNull(email);
    }

    @Test
    void extractRole_Success() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@test.com", "CUSTOMER");
        String role = jwtTokenProvider.extractRole(token);
        assertEquals("CUSTOMER", role);
    }

    @Test
    void extractRole_InvalidToken_ReturnsNull() {
        String role = jwtTokenProvider.extractRole("invalid-token");
        assertNull(role);
    }

    @Test
    void isTokenExpired_NotExpired() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@test.com", "CUSTOMER");
        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_InvalidToken_ReturnsTrue() {
        assertTrue(jwtTokenProvider.isTokenExpired("invalid-token"));
    }
}
