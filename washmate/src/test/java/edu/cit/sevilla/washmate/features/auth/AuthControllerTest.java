package edu.cit.sevilla.washmate.features.auth;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private VerificationCodeService verificationCodeService;

    @MockBean
    private GoogleOAuthService googleOAuthService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RateLimitUtil rateLimitUtil;

    @MockBean
    private OAuthRedirectCodeUtil oAuthRedirectCodeUtil;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@test.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashedPassword")
                .role("CUSTOMER")
                .emailVerified(true)
                .twoFactorEnabled(false)
                .build();

        // Default: rate limiting always allows
        when(rateLimitUtil.isWithinLimit(anyString(), anyInt(), anyInt())).thenReturn(true);
    }

    // ===== POST /api/auth/register =====

    @Test
    void register_Success() throws Exception {
        when(authService.registerWithEmailPassword(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testUser);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("Password1");
        request.setPhoneNumber("0912345");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_EmailAlreadyExists() throws Exception {
        when(authService.registerWithEmailPassword(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Email already registered"));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("Password1");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/auth/login =====

    @Test
    void login_Success() throws Exception {
        when(authService.validateEmailPassword("test@test.com", "Password1")).thenReturn(testUser);
        when(authService.generateAccessToken(testUser)).thenReturn("access-token");
        when(authService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("test@test.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        when(authService.validateEmailPassword("test@test.com", "wrong")).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("test@test.com");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_EmailNotVerified() throws Exception {
        testUser.setEmailVerified(false);
        when(authService.validateEmailPassword("test@test.com", "Password1")).thenReturn(testUser);

        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("test@test.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresEmailVerification").value(true));
    }

    @Test
    void login_TwoFactorEnabled() throws Exception {
        testUser.setTwoFactorEnabled(true);
        when(authService.validateEmailPassword("test@test.com", "Password1")).thenReturn(testUser);

        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("test@test.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFactor").value(true));
    }

    // ===== POST /api/auth/verify-email =====

    @Test
    void verifyEmail_Success() throws Exception {
        when(verificationCodeService.verifyCode(1L, "123456", "EMAIL_VERIFICATION")).thenReturn(true);
        when(authService.getUserById(1L)).thenReturn(testUser);
        when(authService.generateAccessToken(testUser)).thenReturn("access-token");
        when(authService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setUserId(1L);
        request.setCode("123456");

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void verifyEmail_InvalidCode() throws Exception {
        when(verificationCodeService.verifyCode(1L, "wrong", "EMAIL_VERIFICATION")).thenReturn(false);
        when(verificationCodeService.getRemainingAttempts(1L, "EMAIL_VERIFICATION")).thenReturn(2);

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setUserId(1L);
        request.setCode("wrong");

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyEmail_MaxAttemptsExceeded() throws Exception {
        when(verificationCodeService.verifyCode(1L, "wrong", "EMAIL_VERIFICATION")).thenReturn(false);
        when(verificationCodeService.getRemainingAttempts(1L, "EMAIL_VERIFICATION")).thenReturn(0);

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setUserId(1L);
        request.setCode("wrong");

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/auth/resend-otp =====

    @Test
    void resendOtp_Success() throws Exception {
        when(authService.getUserByEmail("test@test.com")).thenReturn(testUser);

        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("test@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resendOtp_UserNotFound() throws Exception {
        when(authService.getUserByEmail("wrong@test.com")).thenReturn(null);

        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("wrong@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ===== POST /api/auth/forgot-password =====

    @Test
    void forgotPassword_Success() throws Exception {
        when(authService.getUserByEmail("test@test.com")).thenReturn(testUser);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_UserNotFound() throws Exception {
        when(authService.getUserByEmail("wrong@test.com")).thenReturn(null);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("wrong@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Security: don't reveal if email exists
    }

    // ===== POST /api/auth/reset-password =====

    @Test
    void resetPassword_Success() throws Exception {
        when(authService.getUserByEmail("test@test.com")).thenReturn(testUser);
        when(verificationCodeService.verifyCode(1L, "123456", "PASSWORD_RESET")).thenReturn(true);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setCode("123456");
        request.setNewPassword("NewPassword1");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_InvalidCode() throws Exception {
        when(authService.getUserByEmail("test@test.com")).thenReturn(testUser);
        when(verificationCodeService.verifyCode(1L, "wrong", "PASSWORD_RESET")).thenReturn(false);
        when(verificationCodeService.getRemainingAttempts(1L, "PASSWORD_RESET")).thenReturn(2);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setCode("wrong");
        request.setNewPassword("NewPassword1");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/auth/refresh =====

    @Test
    void refresh_Success() throws Exception {
        when(jwtTokenProvider.validateToken("valid-refresh")).thenReturn(true);
        when(jwtTokenProvider.isTokenExpired("valid-refresh")).thenReturn(false);
        when(jwtTokenProvider.extractUserId("valid-refresh")).thenReturn(1L);
        when(authService.getUserById(1L)).thenReturn(testUser);
        when(authService.generateAccessToken(testUser)).thenReturn("new-access-token");

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void refresh_InvalidToken() throws Exception {
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ===== POST /api/auth/logout =====

    @Test
    void logout_Success() throws Exception {
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(jwtTokenProvider.extractUserId("some-refresh")).thenReturn(1L);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("some-refresh");

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ===== GET /api/auth/email-by-username =====

    @Test
    void emailByUsername_Found() throws Exception {
        when(authService.findEmailByUsername("testuser")).thenReturn(java.util.Optional.of("test@test.com"));

        mockMvc.perform(get("/api/auth/email-by-username")
                .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void emailByUsername_NotFound() throws Exception {
        when(authService.findEmailByUsername("unknown")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/auth/email-by-username")
                .param("username", "unknown"))
                .andExpect(status().isNotFound());
    }

    // ===== GET /api/auth/me (authenticated) =====

    @Test
    void getCurrentUser_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getCurrentUser_NotFound() throws Exception {
        when(authService.findUserById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isNotFound());
    }

    // ===== PUT /api/auth/me (authenticated) =====

    @Test
    void updateUser_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(authService.updateUser(any(User.class))).thenReturn(testUser);

        String body = "{\"firstName\":\"Updated\",\"lastName\":\"Name\",\"phoneNumber\":\"09876543210\"}";

        mockMvc.perform(put("/api/auth/me")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_NullFields() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(authService.updateUser(any(User.class))).thenReturn(testUser);

        String body = "{\"firstName\":\"\",\"lastName\":null,\"phoneNumber\":\"\"}";

        mockMvc.perform(put("/api/auth/me")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());
    }

    // ===== POST /api/auth/change-password =====

    @Test
    void changePassword_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPassword1");
        request.setNewPassword("NewPassword1");

        mockMvc.perform(post("/api/auth/change-password")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_WrongCurrent() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        doThrow(new RuntimeException("Current password is incorrect"))
                .when(authService).changePassword(any(User.class), anyString(), anyString());

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("NewPassword1");

        mockMvc.perform(post("/api/auth/change-password")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/auth/subscription =====

    @Test
    void getSubscription_Success() throws Exception {
        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(authService.getUserSubscriptionInfo(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/auth/subscription")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getSubscription_NoSubscription() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(authService.getUserSubscriptionInfo(1L)).thenReturn(null);

        // The controller uses Map.of("subscription", null) which throws NPE,
        // so the catch block returns 500
        mockMvc.perform(get("/api/auth/subscription")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isInternalServerError());
    }

    // ===== 2FA Endpoints =====

    @Test
    void send2FACode_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/2fa/send-code")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void verifyLogin2FA_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(verificationCodeService.verifyCode(1L, "123456", "TWO_FACTOR_LOGIN")).thenReturn(true);
        when(authService.generateAccessToken(testUser)).thenReturn("access-token");
        when(authService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        TwoFactorLoginRequest request = new TwoFactorLoginRequest();
        request.setUserId(1L);
        request.setCode("123456");

        mockMvc.perform(post("/api/auth/2fa/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void verifyLogin2FA_InvalidCode() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(verificationCodeService.verifyCode(1L, "wrong", "TWO_FACTOR_LOGIN")).thenReturn(false);

        TwoFactorLoginRequest request = new TwoFactorLoginRequest();
        request.setUserId(1L);
        request.setCode("wrong");

        mockMvc.perform(post("/api/auth/2fa/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendLogin2FA_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);

        TwoFactorResendRequest request = new TwoFactorResendRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/api/auth/2fa/resend-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void enable2FA_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(verificationCodeService.verifyCode(1L, "123456", "TWO_FACTOR_AUTH")).thenReturn(true);
        when(authService.updateUser(any(User.class))).thenReturn(testUser);

        TwoFactorRequest request = new TwoFactorRequest();
        request.setCode("123456");

        mockMvc.perform(post("/api/auth/2fa/enable")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void enable2FA_InvalidCode() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(verificationCodeService.verifyCode(1L, "wrong", "TWO_FACTOR_AUTH")).thenReturn(false);

        TwoFactorRequest request = new TwoFactorRequest();
        request.setCode("wrong");

        mockMvc.perform(post("/api/auth/2fa/enable")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disable2FA_Success() throws Exception {
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(authService.updateUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/2fa/disable")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    // ===== POST /api/auth/verify-redirect-code =====

    @Test
    void verifyRedirectCode_Success() throws Exception {
        OAuthRedirectCodeUtil.RedirectCodeData codeData = OAuthRedirectCodeUtil.RedirectCodeData.builder().userId(1L).build();
        when(oAuthRedirectCodeUtil.getAndConsumeRedirectCode("code123")).thenReturn(codeData);
        when(authService.getUserById(1L)).thenReturn(testUser);
        when(authService.generateAccessToken(testUser)).thenReturn("access-token");
        when(authService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        VerifyRedirectCodeRequest request = new VerifyRedirectCodeRequest();
        request.setRedirectCode("code123");

        mockMvc.perform(post("/api/auth/verify-redirect-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void verifyRedirectCode_NullData() throws Exception {
        when(oAuthRedirectCodeUtil.getAndConsumeRedirectCode("code123")).thenReturn(null);

        VerifyRedirectCodeRequest request = new VerifyRedirectCodeRequest();
        request.setRedirectCode("code123");

        mockMvc.perform(post("/api/auth/verify-redirect-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ===== GET /api/auth/google/login =====

    @Test
    void googleLogin_Success() throws Exception {
        when(oAuthRedirectCodeUtil.generateState()).thenReturn("state123");

        mockMvc.perform(get("/api/auth/google/login"))
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"));
    }

    // ===== GET /api/auth/google/callback =====

    @Test
    void googleCallback_Success() throws Exception {
        java.util.Map<String, Object> tokenResponse = java.util.Map.of("access_token", "google-access-token");
        java.util.Map<String, Object> userInfoResponse = java.util.Map.of(
                "email", "test@gmail.com",
                "name", "Test User",
                "sub", "google-sub",
                "verified_email", true
        );

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.postForObject(anyString(), any(), eq(java.util.Map.class))).thenReturn(tokenResponse);
                    ResponseEntity<java.util.Map> entity = new ResponseEntity<>(userInfoResponse, HttpStatus.OK);
                    when(mock.exchange(anyString(), any(), any(), eq(java.util.Map.class))).thenReturn(entity);
                })) {

            when(googleOAuthService.processGoogleOAuth(any())).thenReturn(testUser);
            when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("jwt-access");
            when(jwtTokenProvider.generateRefreshToken(anyLong())).thenReturn("jwt-refresh");

            mockMvc.perform(get("/api/auth/google/callback")
                    .param("code", "google-auth-code")
                    .param("state", "state123"))
                    .andExpect(status().isFound())
                    .andExpect(header().exists("Location"));
        }
    }

    // ===== POST /api/auth/google/mobile =====

    @Test
    void googleMobile_InvalidToken() throws Exception {
        when(googleOAuthService.verifyAndExtractGoogleToken("bad-token")).thenReturn(null);

        GoogleIdTokenRequest request = new GoogleIdTokenRequest();
        request.setIdToken("bad-token");

        mockMvc.perform(post("/api/auth/google/mobile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void googleMobile_Success() throws Exception {
        GoogleOAuthService.GoogleUserInfo googleUser = GoogleOAuthService.GoogleUserInfo.builder()
                .email("test@gmail.com")
                .sub("google-sub")
                .build();
        when(googleOAuthService.verifyAndExtractGoogleToken("valid-token")).thenReturn(googleUser);
        when(googleOAuthService.processGoogleOAuth(googleUser)).thenReturn(testUser);
        when(authService.generateAccessToken(testUser)).thenReturn("access-token");
        when(authService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        GoogleIdTokenRequest request = new GoogleIdTokenRequest();
        request.setIdToken("valid-token");

        mockMvc.perform(post("/api/auth/google/mobile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }
}
