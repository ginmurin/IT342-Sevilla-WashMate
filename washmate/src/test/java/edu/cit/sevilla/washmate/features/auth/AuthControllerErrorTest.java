package edu.cit.sevilla.washmate.features.auth;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import edu.cit.sevilla.washmate.features.users.User;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private VerificationCodeService verificationCodeService;

    @MockBean
    private RateLimitUtil rateLimitUtil;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private OAuthRedirectCodeUtil oAuthRedirectCodeUtil;

    @MockBean
    private GoogleOAuthService googleOAuthService;

    @BeforeEach
    void setUp() {
        when(rateLimitUtil.isWithinLimit(anyString(), anyInt(), anyInt())).thenReturn(true);
    }

    @Test
    void register_RateLimited() throws Exception {
        doReturn(false).when(rateLimitUtil).isWithinLimit(anyString(), anyInt(), anyInt());
        
        User dummyUser = new User();
        dummyUser.setUserId(1L);
        dummyUser.setEmail("test@test.com");
        dummyUser.setUsername("testuser");
        dummyUser.setEmailVerified(false);
        when(authService.registerWithEmailPassword(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(dummyUser);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("Password1");
        request.setPhoneNumber("1234567890");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void login_RateLimited() throws Exception {
        when(rateLimitUtil.isWithinLimit(anyString(), anyInt(), anyInt())).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("test@test.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void verifyEmail_RateLimited() throws Exception {
        when(rateLimitUtil.isWithinLimit(anyString(), anyInt(), anyInt())).thenReturn(false);

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setUserId(1L);
        request.setCode("123456");

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void googleCallback_TokenResponseNull() throws Exception {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.postForObject(anyString(), any(), eq(java.util.Map.class))).thenReturn(null);
                })) {

            mockMvc.perform(get("/api/auth/google/callback")
                    .param("code", "google-auth-code")
                    .param("state", "state123"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void googleCallback_GoogleUserNull() throws Exception {
        java.util.Map<String, Object> tokenResponse = java.util.Map.of("access_token", "google-access-token");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.postForObject(anyString(), any(), eq(java.util.Map.class))).thenReturn(tokenResponse);
                    when(mock.exchange(anyString(), any(), any(), eq(java.util.Map.class))).thenReturn(null);
                })) {

            mockMvc.perform(get("/api/auth/google/callback")
                    .param("code", "google-auth-code")
                    .param("state", "state123"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void verifyRedirectCode_UserNotFound() throws Exception {
        OAuthRedirectCodeUtil.RedirectCodeData codeData = OAuthRedirectCodeUtil.RedirectCodeData.builder()
                .userId(1L)
                .build();
        when(oAuthRedirectCodeUtil.getAndConsumeRedirectCode("code123")).thenReturn(codeData);
        when(authService.getUserById(1L)).thenReturn(null);

        VerifyRedirectCodeRequest request = new VerifyRedirectCodeRequest();
        request.setRedirectCode("code123");

        mockMvc.perform(post("/api/auth/verify-redirect-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifyRedirectCode_Exception() throws Exception {
        when(oAuthRedirectCodeUtil.getAndConsumeRedirectCode(anyString()))
                .thenThrow(new RuntimeException("DB down"));

        VerifyRedirectCodeRequest request = new VerifyRedirectCodeRequest();
        request.setRedirectCode("code123");

        mockMvc.perform(post("/api/auth/verify-redirect-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void register_RuntimeException() throws Exception {
        when(authService.registerWithEmailPassword(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Custom error"));

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

    @Test
    void register_GenericException() throws Exception {
        when(authService.registerWithEmailPassword(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Generic error"));

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

    @Test
    void login_Exception() throws Exception {
        when(authService.validateEmailPassword(anyString(), anyString()))
                .thenThrow(new RuntimeException("DB down"));

        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("test@test.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void verifyEmail_Exception() throws Exception {
        when(verificationCodeService.verifyCode(anyLong(), anyString(), anyString()))
                .thenThrow(new RuntimeException("DB down"));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setUserId(1L);
        request.setCode("123456");

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void resendOtp_Exception() throws Exception {
        when(authService.getUserByEmail(anyString()))
                .thenThrow(new RuntimeException("DB down"));

        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("test@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_Exception() throws Exception {
        when(authService.getUserByEmail(anyString()))
                .thenThrow(new RuntimeException("DB down"));

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void resetPassword_Exception() throws Exception {
        when(authService.getUserByEmail(anyString()))
                .thenThrow(new RuntimeException("DB down"));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setCode("123456");
        request.setNewPassword("NewPwd1");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_Exception() throws Exception {
        when(jwtTokenProvider.validateToken(anyString()))
                .thenThrow(new RuntimeException("DB down"));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void logout_Exception() throws Exception {
        when(jwtTokenProvider.extractUserId(anyString()))
                .thenThrow(new RuntimeException("DB down"));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token");

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Controller catches everything and returns ok for logout
    }

    @Test
    void getCurrentUser_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUser_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        String body = "{\"firstName\":\"Updated\"}";

        mockMvc.perform(put("/api/auth/me")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void changePassword_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new IllegalArgumentException("DB down"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new");

        mockMvc.perform(post("/api/auth/change-password")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSubscription_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/auth/subscription")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void send2FACode_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(post("/api/auth/2fa/send-code")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void verifyLogin2FA_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        TwoFactorLoginRequest request = new TwoFactorLoginRequest();
        request.setUserId(1L);
        request.setCode("123");

        mockMvc.perform(post("/api/auth/2fa/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void resendLogin2FA_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new IllegalArgumentException("DB down"));

        TwoFactorResendRequest request = new TwoFactorResendRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/api/auth/2fa/resend-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // IllegalArgumentException mapped to 400
    }

    @Test
    void resendLogin2FA_RuntimeException() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        TwoFactorResendRequest request = new TwoFactorResendRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/api/auth/2fa/resend-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // RuntimeException mapped to 400
    }

    @Test
    void enable2FA_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        TwoFactorRequest request = new TwoFactorRequest();
        request.setCode("123");

        mockMvc.perform(post("/api/auth/2fa/enable")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void disable2FA_Exception() throws Exception {
        when(authService.findUserById(anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(post("/api/auth/2fa/disable")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isInternalServerError());
    }



    @Test
    void googleMobile_Exception() throws Exception {
        when(googleOAuthService.verifyAndExtractGoogleToken(anyString()))
                .thenThrow(new RuntimeException("DB down"));

        GoogleIdTokenRequest request = new GoogleIdTokenRequest();
        request.setIdToken("token");

        mockMvc.perform(post("/api/auth/google/mobile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
