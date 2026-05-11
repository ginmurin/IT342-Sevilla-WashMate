package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.subscriptions.SubscriptionService;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private GoogleOAuthService googleOAuthService;

    private User user;
    private GoogleOAuthService.GoogleUserInfo googleUser;
    private Subscription freePlan;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .email("test@gmail.com")
                .firstName("Test")
                .lastName("User")
                .oauthId("google-sub-123")
                .oauthProvider("GOOGLE")
                .emailVerified(true)
                .role("CUSTOMER")
                .build();

        googleUser = GoogleOAuthService.GoogleUserInfo.builder()
                .email("test@gmail.com")
                .name("Test User")
                .givenName("Test")
                .familyName("User")
                .sub("google-sub-123")
                .emailVerified(true)
                .build();

        freePlan = Subscription.builder()
                .subscriptionId(1L)
                .planType("FREE")
                .build();
    }

    @Test
    void processGoogleOAuth_ExistingByGoogleId() {
        when(userRepository.findByOauthId("google-sub-123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = googleOAuthService.processGoogleOAuth(googleUser);

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
    }

    @Test
    void processGoogleOAuth_ExistingByEmail_LinkGoogle() {
        User emailUser = User.builder()
                .userId(2L)
                .email("test@gmail.com")
                .firstName("Test")
                .lastName("User")
                .role("CUSTOMER")
                .build();

        when(userRepository.findByOauthId("google-sub-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(emailUser));
        when(userRepository.save(any(User.class))).thenReturn(emailUser);

        User result = googleOAuthService.processGoogleOAuth(googleUser);

        assertNotNull(result);
        assertEquals("GOOGLE", emailUser.getOauthProvider());
        assertTrue(emailUser.getEmailVerified());
    }

    @Test
    void processGoogleOAuth_ExistingByEmail_DifferentProvider_ThrowsException() {
        User emailUser = User.builder()
                .userId(2L)
                .email("test@gmail.com")
                .oauthProvider("FACEBOOK")
                .role("CUSTOMER")
                .build();

        when(userRepository.findByOauthId("google-sub-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(emailUser));

        assertThrows(RuntimeException.class, () ->
                googleOAuthService.processGoogleOAuth(googleUser));
    }

    @Test
    void processGoogleOAuth_NewUser() {
        when(userRepository.findByOauthId("google-sub-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = googleOAuthService.processGoogleOAuth(googleUser);

        assertNotNull(result);
    }

    @Test
    void processGoogleOAuth_NewUser_NullNames() {
        googleUser.setGivenName(null);
        googleUser.setFamilyName(null);

        when(userRepository.findByOauthId("google-sub-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = googleOAuthService.processGoogleOAuth(googleUser);

        assertNotNull(result);
    }

    @Test
    void verifyAndExtractGoogleToken_Success() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@gmail.com");
        payload.set("name", "Test User");
        payload.set("given_name", "Test");
        payload.set("family_name", "User");
        payload.set("picture", "http://example.com/pic.jpg");
        payload.setSubject("google-sub");
        payload.setEmailVerified(true);

        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        when(mockToken.getPayload()).thenReturn(payload);

        try (MockedConstruction<GoogleIdTokenVerifier.Builder> mockedBuilder = mockConstruction(GoogleIdTokenVerifier.Builder.class,
                (mock, context) -> {
                    when(mock.setAudience(any())).thenReturn(mock);
                    GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
                    when(mockVerifier.verify(anyString())).thenReturn(mockToken);
                    when(mock.build()).thenReturn(mockVerifier);
                })) {

            GoogleOAuthService.GoogleUserInfo result = googleOAuthService.verifyAndExtractGoogleToken("valid-token");

            assertNotNull(result);
            assertEquals("test@gmail.com", result.getEmail());
            assertEquals("google-sub", result.getSub());
        }
    }

    @Test
    void verifyAndExtractGoogleToken_InvalidToken_ReturnsNull() {
        // Passing an invalid token should return null since verification will fail
        GoogleOAuthService.GoogleUserInfo result = googleOAuthService.verifyAndExtractGoogleToken("invalid-token");

        assertNull(result);
    }

    @Test
    void verifyAndExtractGoogleToken_Exception_ReturnsNull() throws Exception {
        try (MockedConstruction<GoogleIdTokenVerifier.Builder> mockedBuilder = mockConstruction(GoogleIdTokenVerifier.Builder.class,
                (mock, context) -> {
                    when(mock.setAudience(any())).thenReturn(mock);
                    GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
                    when(mockVerifier.verify(anyString())).thenThrow(new RuntimeException("Verify failed"));
                    when(mock.build()).thenReturn(mockVerifier);
                })) {

            GoogleOAuthService.GoogleUserInfo result = googleOAuthService.verifyAndExtractGoogleToken("token");

            assertNull(result);
        }
    }

    @Test
    void processGoogleOAuth_RuntimeException_ThrowsEx() {
        when(userRepository.findByOauthId(anyString())).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () ->
                googleOAuthService.processGoogleOAuth(googleUser));
    }
}
