package edu.cit.sevilla.washmate.controller;

import edu.cit.sevilla.washmate.config.GoogleOAuthConfig;
import edu.cit.sevilla.washmate.dto.*;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.service.AuthService;
import edu.cit.sevilla.washmate.service.GoogleOAuthService;
import edu.cit.sevilla.washmate.service.VerificationCodeService;
import edu.cit.sevilla.washmate.util.JwtTokenProvider;
import edu.cit.sevilla.washmate.util.OAuthRedirectCodeUtil;
import edu.cit.sevilla.washmate.util.RateLimitUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;
    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitUtil rateLimitUtil;
    private final OAuthRedirectCodeUtil oAuthRedirectCodeUtil;
    private final GoogleOAuthConfig googleOAuthConfig;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET:}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:http://localhost:8080/api/auth/google/callback}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri:https://oauth2.googleapis.com/token}")
    private String googleTokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri:https://www.googleapis.com/oauth2/v3/userinfo}")
    private String googleUserInfoUri;

    private static final int LOGIN_RATE_LIMIT = 5;
    private static final int LOGIN_RATE_WINDOW = 15;
    private static final int REGISTER_RATE_LIMIT = 3;
    private static final int REGISTER_RATE_WINDOW = 15;
    private static final int OTP_RATE_LIMIT = 10;
    private static final int OTP_RATE_WINDOW = 15;
    private static final int RESEND_RATE_LIMIT = 3;
    private static final int RESEND_RATE_WINDOW_HOURS = 1;

    /**
     * POST /api/auth/register
     * Register new user with email and password
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Rate limiting: Max 3 requests per 15 minutes per IP (using email as key)
            String rateLimitKey = "register:" + request.getEmail();
            if (!rateLimitUtil.isWithinLimit(rateLimitKey, REGISTER_RATE_LIMIT, REGISTER_RATE_WINDOW)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many registration attempts. Please try again later."));
            }

            // Register user
            User user = authService.registerWithEmailPassword(
                    request.getEmail(),
                    request.getUsername(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPassword(),
                    request.getPhoneNumber()
            );

            // Generate and send OTP
            verificationCodeService.generateAndSendCode(
                    user.getUserId(),
                    "EMAIL_VERIFICATION",
                    user.getEmail(),
                    user.getUsername()
            );

            rateLimitUtil.incrementAttempt(rateLimitKey, REGISTER_RATE_WINDOW);

            log.info("User registered successfully: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.builder()
                            .userId(user.getUserId())
                            .email(user.getEmail())
                            .requiresEmailVerification(true)
                            .message("Registration successful. Please verify your email with the OTP sent.")
                            .build());
        } catch (RuntimeException ex) {
            log.error("Registration failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * POST /api/auth/login
     * Login with email/username and password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Rate limiting: Max 5 attempts per 15 minutes
            String rateLimitKey = "login:" + request.getEmailOrUsername();
            if (!rateLimitUtil.isWithinLimit(rateLimitKey, LOGIN_RATE_LIMIT, LOGIN_RATE_WINDOW)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many login attempts. Please try again later."));
            }

            // Validate credentials
            User user = authService.validateEmailPassword(request.getEmailOrUsername(), request.getPassword());
            if (user == null) {
                rateLimitUtil.incrementAttempt(rateLimitKey, LOGIN_RATE_WINDOW);
                log.warn("Invalid credentials for: {}", request.getEmailOrUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email/username or password"));
            }

            // Check if email is verified
            if (!user.getEmailVerified()) {
                // Send OTP for verification
                verificationCodeService.generateAndSendCode(
                        user.getUserId(),
                        "EMAIL_VERIFICATION",
                        user.getEmail(),
                        user.getUsername()
                );

                log.info("Email not verified for user: {}, OTP sent", user.getEmail());
                return ResponseEntity.ok(AuthResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .requiresEmailVerification(true)
                        .message("Email not verified. OTP sent to your email.")
                        .build());
            }

            // Generate tokens
            String accessToken = authService.generateAccessToken(user);
            String refreshToken = authService.generateRefreshToken(user);

            rateLimitUtil.reset(rateLimitKey);

            log.info("User logged in successfully: {}", user.getEmail());
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(900L) // 15 minutes
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build());
        } catch (Exception ex) {
            log.error("Login error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed"));
        }
    }

    /**
     * POST /api/auth/verify-email
     * Verify email with OTP code
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            // Rate limiting: Max 10 attempts per 15 minutes
            String rateLimitKey = "verify-email:" + request.getUserId();
            if (!rateLimitUtil.isWithinLimit(rateLimitKey, OTP_RATE_LIMIT, OTP_RATE_WINDOW)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many verification attempts. Please try again later."));
            }

            // Verify code
            boolean isValid = verificationCodeService.verifyCode(request.getUserId(), request.getCode(), "EMAIL_VERIFICATION");
            if (!isValid) {
                int remaining = verificationCodeService.getRemainingAttempts(request.getUserId(), "EMAIL_VERIFICATION");
                rateLimitUtil.incrementAttempt(rateLimitKey, OTP_RATE_WINDOW);

                if (remaining <= 0) {
                    log.warn("Max verification attempts exceeded for user: {}", request.getUserId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of(
                                    "error", "Invalid verification code. Maximum attempts exceeded. Please request a new code.",
                                    "remainingAttempts", 0
                            ));
                }

                log.warn("Invalid verification code for user: {}, remaining attempts: {}", request.getUserId(), remaining);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Invalid verification code",
                                "remainingAttempts", remaining
                        ));
            }

            // Get user and mark email as verified
            User user = authService.getUserById(request.getUserId());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            authService.markEmailAsVerified(request.getUserId());

            // Generate tokens
            String accessToken = authService.generateAccessToken(user);
            String refreshToken = authService.generateRefreshToken(user);

            rateLimitUtil.reset(rateLimitKey);

            log.info("Email verified successfully for user: {}", request.getUserId());
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(900L) // 15 minutes
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build());
        } catch (Exception ex) {
            log.error("Email verification error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Email verification failed"));
        }
    }

    /**
     * POST /api/auth/resend-otp
     * Resend OTP code with cooldown
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        try {
            // Rate limiting: Max 3 requests per hour
            String rateLimitKey = "resend-otp:" + request.getEmail();
            if (!rateLimitUtil.isWithinLimit(rateLimitKey, RESEND_RATE_LIMIT, RESEND_RATE_WINDOW_HOURS * 60)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many resend requests. Please try again later."));
            }

            User user = authService.getUserByEmail(request.getEmail());
            if (user == null) {
                log.warn("Resend OTP request for non-existent user: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            // Resend OTP
            verificationCodeService.resendCode(
                    user.getUserId(),
                    "EMAIL_VERIFICATION",
                    user.getEmail(),
                    user.getUsername()
            );

            rateLimitUtil.incrementAttempt(rateLimitKey, RESEND_RATE_WINDOW_HOURS * 60);

            log.info("OTP resent successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent to your email",
                    "expiresIn", 600 // 10 minutes
            ));
        } catch (RuntimeException ex) {
            log.warn("Resend OTP error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Resend OTP error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to resend OTP"));
        }
    }

    /**
     * POST /api/auth/forgot-password
     * Initiate password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // Rate limiting: Max 3 requests per hour
            String rateLimitKey = "forgot-password:" + request.getEmail();
            if (!rateLimitUtil.isWithinLimit(rateLimitKey, 3, 60)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many password reset requests. Please try again later."));
            }

            User user = authService.getUserByEmail(request.getEmail());
            if (user == null) {
                // Don't reveal if user exists
                log.warn("Password reset request for non-existent user: {}", request.getEmail());
                return ResponseEntity.ok(Map.of("message", "If email exists, password reset code will be sent"));
            }

            // Generate and send reset code
            verificationCodeService.generateAndSendCode(
                    user.getUserId(),
                    "PASSWORD_RESET",
                    user.getEmail(),
                    user.getUsername()
            );

            rateLimitUtil.incrementAttempt(rateLimitKey, 60);

            log.info("Password reset code sent for user: {}", user.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "If the email exists, a password reset code will be sent",
                    "expiresIn", 600 // 10 minutes
            ));
        } catch (Exception ex) {
            log.error("Forgot password error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process password reset request"));
        }
    }

    /**
     * POST /api/auth/reset-password
     * Reset password with code
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // Rate limiting: Max 10 attempts per 15 minutes
            String rateLimitKey = "reset-password:" + request.getEmail();
            if (!rateLimitUtil.isWithinLimit(rateLimitKey, OTP_RATE_LIMIT, OTP_RATE_WINDOW)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many reset attempts. Please try again later."));
            }

            User user = authService.getUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            // Verify reset code
            boolean isValid = verificationCodeService.verifyCode(user.getUserId(), request.getCode(), "PASSWORD_RESET");
            if (!isValid) {
                int remaining = verificationCodeService.getRemainingAttempts(user.getUserId(), "PASSWORD_RESET");
                rateLimitUtil.incrementAttempt(rateLimitKey, OTP_RATE_WINDOW);

                log.warn("Invalid reset code for user: {}, remaining attempts: {}", request.getEmail(), remaining);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Invalid or expired reset code",
                                "remainingAttempts", remaining
                        ));
            }

            // Reset password
            authService.resetPassword(request.getEmail(), request.getNewPassword());

            rateLimitUtil.reset(rateLimitKey);

            log.info("Password reset successfully for user: {}", request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException ex) {
            log.warn("Reset password error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Reset password error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset password"));
        }
    }

    /**
     * POST /api/auth/refresh
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            // Validate refresh token
            if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
                log.warn("Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired refresh token"));
            }

            if (jwtTokenProvider.isTokenExpired(request.getRefreshToken())) {
                log.warn("Refresh token expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token expired"));
            }

            Long userId = jwtTokenProvider.extractUserId(request.getRefreshToken());
            User user = authService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            // Generate new access token
            String newAccessToken = authService.generateAccessToken(user);

            log.info("Access token refreshed for user: {}", userId);
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .expiresIn(900L) // 15 minutes
                    .build());
        } catch (Exception ex) {
            log.error("Token refresh error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to refresh token"));
        }
    }

    /**
     * POST /api/auth/logout
     * Logout user (optional - can invalidate refresh token)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody(required = false) RefreshTokenRequest request) {
        try {
            // Optional: Invalidate refresh token in Redis
            if (request != null && request.getRefreshToken() != null) {
                Long userId = jwtTokenProvider.extractUserId(request.getRefreshToken());
                if (userId != null) {
                    redisTemplate.opsForValue().set("blacklist:" + request.getRefreshToken(), "true", 7, TimeUnit.DAYS);
                    log.info("Refresh token invalidated for user: {}", userId);
                }
            }

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception ex) {
            log.error("Logout error: {}", ex.getMessage());
            return ResponseEntity.ok(Map.of("message", "Logged out"));
        }
    }

    /**
     * GET /api/auth/email-by-username?username=xxx
     * Public endpoint — resolves a username to its email
     */
    @GetMapping("/email-by-username")
    public ResponseEntity<?> emailByUsername(@RequestParam("username") String username) {
        return authService.findEmailByUsername(username)
                .map(email -> ResponseEntity.ok(Map.of("email", email)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No account found with that username")));
    }

    /**
     * GET /api/auth/google/login
     * Initiates Google OAuth - generates state parameter and redirects to Google
     */
    @GetMapping("/google/login")
    public ResponseEntity<?> googleLogin() {
        try {
            // Generate state parameter for CSRF protection
            String state = oAuthRedirectCodeUtil.generateState();
            oAuthRedirectCodeUtil.storeState(state);

            // Build Google authorization URL
            String authorizationUrl = String.format(
                    "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s",
                    URLEncoder.encode(googleClientId, "UTF-8"),
                    URLEncoder.encode(googleRedirectUri, "UTF-8"),
                    URLEncoder.encode("openid email profile", "UTF-8"),
                    URLEncoder.encode(state, "UTF-8")
            );

            log.info("Google OAuth login initiated with state: {}", state);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", authorizationUrl)
                    .build();
        } catch (UnsupportedEncodingException ex) {
            log.error("Error building Google authorization URL: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to initiate Google login"));
        }
    }

    /**
     * GET /api/auth/google/callback
     * Google OAuth callback - handles authorization code and user creation/linking
     * Returns redirect with short-lived code instead of JWT in URL
     */
    @GetMapping("/google/callback")
    public ResponseEntity<?> googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state) {
        try {
            log.info("Google OAuth callback received with code");

            // Exchange authorization code for tokens via Google
            Map<String, Object> tokenResponse = exchangeCodeForTokens(code);
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                log.error("Failed to get access token from Google");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Failed to authenticate with Google"));
            }

            String accessToken = (String) tokenResponse.get("access_token");

            // Get user info using access token
            GoogleOAuthService.GoogleUserInfo googleUser = getUserInfoFromGoogle(accessToken);
            if (googleUser == null) {
                log.error("Failed to get user info from Google");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Failed to get user info from Google"));
            }

            // Process OAuth - create or link user
            User user = googleOAuthService.processGoogleOAuth(googleUser);

            // Generate JWT tokens
            String jwtAccessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole().toString());
            String jwtRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

            log.info("Google OAuth successful for user: {}", user.getEmail());

            // Build query parameters for frontend
            String frontendRedirectUri = googleOAuthConfig.getFrontendRedirectUri();
            if (frontendRedirectUri == null) {
                frontendRedirectUri = "http://localhost:5173/auth/callback";
            }

            // Redirect to frontend with tokens and user info
            String redirectUrl = String.format(
                    "%s?accessToken=%s&refreshToken=%s&userId=%d&username=%s&firstName=%s&lastName=%s&email=%s&role=%s",
                    frontendRedirectUri,
                    URLEncoder.encode(jwtAccessToken, "UTF-8"),
                    URLEncoder.encode(jwtRefreshToken, "UTF-8"),
                    user.getUserId(),
                    URLEncoder.encode(user.getUsername() != null ? user.getUsername() : "", "UTF-8"),
                    URLEncoder.encode(user.getFirstName(), "UTF-8"),
                    URLEncoder.encode(user.getLastName(), "UTF-8"),
                    URLEncoder.encode(user.getEmail(), "UTF-8"),
                    URLEncoder.encode(user.getRole().toString(), "UTF-8")
            );

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();

        } catch (RuntimeException ex) {
            log.error("Google OAuth error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Google OAuth callback error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google OAuth processing failed"));
        }
    }

    /**
     * Get user info from Google using access token
     */
    private GoogleOAuthService.GoogleUserInfo getUserInfoFromGoogle(String accessToken) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            Map<String, Object> response = restTemplate.exchange(
                    googleUserInfoUri,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            ).getBody();

            if (response == null) {
                log.error("Null response from Google userinfo endpoint");
                return null;
            }

            return GoogleOAuthService.GoogleUserInfo.builder()
                    .email((String) response.get("email"))
                    .name((String) response.get("name"))
                    .givenName((String) response.get("given_name"))
                    .familyName((String) response.get("family_name"))
                    .picture((String) response.get("picture"))
                    .sub((String) response.get("sub"))
                    .emailVerified((Boolean) response.getOrDefault("verified_email", true))
                    .build();
        } catch (Exception ex) {
            log.error("Error getting user info from Google: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Exchange authorization code for Google tokens
     */
    private Map<String, Object> exchangeCodeForTokens(String code) {
        try {
            // Use RestTemplate to exchange code for tokens
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

            // Build form-encoded request body
            org.springframework.util.MultiValueMap<String, String> requestBody = new org.springframework.util.LinkedMultiValueMap<>();
            requestBody.add("code", code);
            requestBody.add("client_id", googleClientId);
            requestBody.add("client_secret", googleClientSecret);
            requestBody.add("redirect_uri", googleRedirectUri);
            requestBody.add("grant_type", "authorization_code");

            // Set content type to form-encoded
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            log.info("Exchanging code for tokens with client_id: {}", googleClientId);

            Map<String, Object> response = restTemplate.postForObject(
                    googleTokenUri,
                    entity,
                    Map.class
            );

            if (response == null) {
                log.error("Null response from Google token endpoint");
                return null;
            }

            log.info("Google token response keys: {}", response.keySet());

            // Check for error in response
            if (response.containsKey("error")) {
                log.error("Google token error: {}", response.get("error"));
                return null;
            }

            return response;
        } catch (Exception ex) {
            log.error("Error exchanging code for tokens: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * POST /api/auth/verify-redirect-code
     * Exchange redirect code for JWT tokens (called by frontend after OAuth callback)
     */
    @PostMapping("/verify-redirect-code")
    public ResponseEntity<?> verifyRedirectCode(@Valid @RequestBody VerifyRedirectCodeRequest request) {
        try {
            OAuthRedirectCodeUtil.RedirectCodeData codeData = oAuthRedirectCodeUtil
                    .getAndConsumeRedirectCode(request.getRedirectCode());

            if (codeData == null) {
                log.warn("Invalid or expired redirect code");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Redirect code expired or invalid"));
            }

            // Get user and generate fresh tokens
            User user = authService.getUserById(codeData.getUserId());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            String accessToken = authService.generateAccessToken(user);
            String refreshToken = authService.generateRefreshToken(user);

            log.info("Redirect code verified successfully for user: {}", codeData.getUserId());
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(900L) // 15 minutes
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build());
        } catch (Exception ex) {
            log.error("Redirect code verification error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify redirect code"));
        }
    }

    /**
     * POST /api/auth/google/mobile
     * Simpler endpoint for mobile - accepts Google ID token directly
     * This avoids browser redirect complexities on mobile
     */
    @PostMapping("/google/mobile")
    public ResponseEntity<?> googleMobile(@Valid @RequestBody GoogleIdTokenRequest request) {
        try {
            // Verify Google ID token
            GoogleOAuthService.GoogleUserInfo googleUser = googleOAuthService
                    .verifyAndExtractGoogleToken(request.getIdToken());

            if (googleUser == null) {
                log.warn("Invalid Google ID token from mobile");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid Google ID token"));
            }

            // Process Google OAuth (create or link user)
            User user = googleOAuthService.processGoogleOAuth(googleUser);

            // Generate tokens
            String accessToken = authService.generateAccessToken(user);
            String refreshToken = authService.generateRefreshToken(user);

            log.info("Mobile Google OAuth successful for user: {}", googleUser.getEmail());
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(900L) // 15 minutes
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build());
        } catch (RuntimeException ex) {
            log.warn("Mobile Google OAuth error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Mobile Google OAuth error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Mobile Google OAuth failed"));
        }
    }
}
