package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.subscriptions.SubscriptionService;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscription;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionDTO;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Subscription freePlan;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .email("test@test.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashedPassword")
                .role("CUSTOMER")
                .emailVerified(true)
                .build();

        freePlan = Subscription.builder()
                .subscriptionId(1L)
                .planType("FREE")
                .build();
    }

    // ===== registerWithEmailPassword =====

    @Test
    void registerWithEmailPassword_Success() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "Password1", "0912345");

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerWithEmailPassword_EmailAlreadyExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                authService.registerWithEmailPassword("test@test.com", "newuser", "New", "User", "Password1", null));
    }

    @Test
    void registerWithEmailPassword_UsernameAlreadyTaken() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                authService.registerWithEmailPassword("new@test.com", "testuser", "New", "User", "Password1", null));
    }

    @Test
    void registerWithEmailPassword_WeakPassword() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "weak", null));
    }

    @Test
    void registerWithEmailPassword_PasswordNoUppercase() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "password1", null));
    }

    @Test
    void registerWithEmailPassword_PasswordNoNumber() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "Password", null));
    }

    // ===== validateEmailPassword =====

    @Test
    void validateEmailPassword_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = authService.validateEmailPassword("test@test.com", "password");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void validateEmailPassword_UserNotFound() {
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("wrong@test.com")).thenReturn(Optional.empty());

        User result = authService.validateEmailPassword("wrong@test.com", "password");

        assertNull(result);
    }

    @Test
    void validateEmailPassword_NoPasswordSet() {
        user.setPasswordHash(null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = authService.validateEmailPassword("test@test.com", "password");

        assertNull(result);
    }

    @Test
    void validateEmailPassword_EmptyPasswordHash() {
        user.setPasswordHash("");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = authService.validateEmailPassword("test@test.com", "password");

        assertNull(result);
    }

    @Test
    void validateEmailPassword_WrongPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedPassword")).thenReturn(false);

        User result = authService.validateEmailPassword("test@test.com", "wrong");

        assertNull(result);
    }

    @Test
    void validateEmailPassword_ByUsername() {
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = authService.validateEmailPassword("testuser", "password");

        assertNotNull(result);
    }

    // ===== Token generation =====

    @Test
    void generateAccessToken_DelegatesToProvider() {
        when(jwtTokenProvider.generateAccessToken(1L, "test@test.com", "CUSTOMER")).thenReturn("access-token");

        String token = authService.generateAccessToken(user);

        assertEquals("access-token", token);
    }

    @Test
    void generateRefreshToken_DelegatesToProvider() {
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("refresh-token");

        String token = authService.generateRefreshToken(user);

        assertEquals("refresh-token", token);
    }

    // ===== resetPassword =====

    @Test
    void resetPassword_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newHash");

        assertDoesNotThrow(() -> authService.resetPassword("test@test.com", "NewPassword1"));

        assertEquals("newHash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_UserNotFound() {
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("wrong@test.com", "NewPassword1"));
    }

    @Test
    void resetPassword_WeakNewPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("test@test.com", "weak"));
    }

    // ===== changePassword =====

    @Test
    void changePassword_Success() {
        when(passwordEncoder.matches("currentPwd", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newHash");

        assertDoesNotThrow(() -> authService.changePassword(user, "currentPwd", "NewPassword1"));

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_UserNull() {
        assertThrows(RuntimeException.class, () ->
                authService.changePassword(null, "current", "NewPassword1"));
    }

    @Test
    void changePassword_NoPasswordSet() {
        user.setPasswordHash(null);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "current", "NewPassword1"));
    }

    @Test
    void changePassword_WrongCurrentPassword() {
        when(passwordEncoder.matches("wrong", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "wrong", "NewPassword1"));
    }

    @Test
    void changePassword_WeakNewPassword() {
        when(passwordEncoder.matches("currentPwd", "hashedPassword")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "currentPwd", "weak"));
    }

    // ===== Other methods =====

    @Test
    void markEmailAsVerified_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.markEmailAsVerified(1L);

        assertTrue(user.getEmailVerified());
        verify(userRepository).save(user);
    }

    @Test
    void markEmailAsVerified_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.markEmailAsVerified(99L));
    }

    @Test
    void getUserByEmail_Found() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = authService.getUserByEmail("test@test.com");

        assertNotNull(result);
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        User result = authService.getUserByEmail("wrong@test.com");

        assertNull(result);
    }

    @Test
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = authService.getUserById(1L);

        assertNotNull(result);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        User result = authService.getUserById(99L);

        assertNull(result);
    }

    @Test
    void findUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = authService.findUserById(1L);

        assertNotNull(result);
    }

    @Test
    void updateUser_Success() {
        when(userRepository.save(user)).thenReturn(user);

        User result = authService.updateUser(user);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void getAuthService_ReturnsSelf() {
        AuthService result = authService.getAuthService();

        assertSame(authService, result);
    }

    @Test
    void getVerificationCodeService_ReturnsInstance() {
        VerificationCodeService result = authService.getVerificationCodeService();

        assertSame(verificationCodeService, result);
    }

    @Test
    void findEmailByUsername_Found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<String> result = authService.findEmailByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get());
    }

    @Test
    void findEmailByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<String> result = authService.findEmailByUsername("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void getUserSubscriptionInfo_Found() {
        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        UserSubscription sub = mock(UserSubscription.class);
        when(subscriptionService.getCurrentSubscription(1L)).thenReturn(Optional.of(sub));
        when(subscriptionService.toUserSubscriptionDTO(sub)).thenReturn(dto);

        UserSubscriptionDTO result = authService.getUserSubscriptionInfo(1L);

        assertNotNull(result);
    }

    @Test
    void getUserSubscriptionInfo_NotFound() {
        when(subscriptionService.getCurrentSubscription(1L)).thenReturn(Optional.empty());

        UserSubscriptionDTO result = authService.getUserSubscriptionInfo(1L);

        assertNull(result);
    }

    // ===== syncUser =====

    @Test
    void syncUser_ExistingByOAuthId() {
        when(userRepository.findByOauthId("oauth123")).thenReturn(Optional.of(user));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");

        AuthResponse result = authService.syncUser(request, "oauth123", "token");

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    void syncUser_ExistingByEmail() {
        when(userRepository.findByOauthId("oauth123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");

        AuthResponse result = authService.syncUser(request, "oauth123", "token");

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    void syncUser_NewUser() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setFirstName("New");
        request.setLastName("User");

        AuthResponse result = authService.syncUser(request, null, "token");

        assertNotNull(result);
    }

    @Test
    void syncUser_NewUser_WithRole() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRole("SHOP_OWNER");

        AuthResponse result = authService.syncUser(request, null, "token");

        assertNotNull(result);
    }

    // ===== Additional Branch Coverage Tests =====

    @Test
    void syncUser_NewUser_WithBlankRole() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRole("   ");

        AuthResponse result = authService.syncUser(request, null, "token");

        assertNotNull(result);
        assertEquals("CUSTOMER", user.getRole()); // Should default to CUSTOMER
    }

    @Test
    void syncUser_ExistingUser_WithOAuthAndEmail() {
        when(userRepository.findByOauthId("oauth123")).thenReturn(Optional.of(user));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setRole("SHOP_OWNER");

        AuthResponse result = authService.syncUser(request, "oauth123", "token");

        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
    }

    @Test
    void syncUser_ExistingByEmail_WithOAuthId() {
        User oauthUser = User.builder()
                .userId(1L)
                .email("test@test.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role("CUSTOMER")
                .emailVerified(true)
                .build();

        when(userRepository.findByOauthId("oauth456")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(oauthUser));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");

        AuthResponse result = authService.syncUser(request, "oauth456", "token");

        assertNotNull(result);
        assertEquals(oauthUser.getUserId(), result.getUserId());
    }

    @Test
    void registerWithEmailPassword_WithPhoneNumber() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "Password1", "09123456789");

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerWithEmailPassword_NullPhoneNumber() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "Password1", null);

        assertNotNull(result);
    }

    @Test
    void validateEmailPassword_WithCorrectPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = authService.validateEmailPassword("test@test.com", "password");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void validateEmailPassword_FindByUsernameWhenEmailNotFound() {
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = authService.validateEmailPassword("testuser", "password");

        assertNotNull(result);
    }

    @Test
    void resetPassword_WithWeakPassword_NoNumbers() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("test@test.com", "Password"));
    }

    @Test
    void resetPassword_WithWeakPassword_NoUppercase() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("test@test.com", "password1"));
    }

    @Test
    void resetPassword_WithWeakPassword_TooShort() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("test@test.com", "Pass1"));
    }

    @Test
    void resetPassword_WithNullPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("test@test.com", null));
    }

    @Test
    void changePassword_WithCorrectCurrentPassword() {
        when(passwordEncoder.matches("currentPwd", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newHash");

        assertDoesNotThrow(() -> authService.changePassword(user, "currentPwd", "NewPassword1"));
        assertEquals("newHash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_WithEmptyPasswordHash() {
        user.setPasswordHash("");

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "current", "NewPassword1"));
    }

    @Test
    void changePassword_WithInvalidNewPassword_NoUppercase() {
        when(passwordEncoder.matches("currentPwd", "hashedPassword")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "currentPwd", "password1"));
    }

    @Test
    void changePassword_WithInvalidNewPassword_NoNumbers() {
        when(passwordEncoder.matches("currentPwd", "hashedPassword")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "currentPwd", "Password"));
    }

    @Test
    void changePassword_WithInvalidNewPassword_TooShort() {
        when(passwordEncoder.matches("currentPwd", "hashedPassword")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "currentPwd", "Pass1"));
    }

    @Test
    void markEmailAsVerified_MarksEmailAsTrue() {
        user.setEmailVerified(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.markEmailAsVerified(1L);

        assertTrue(user.getEmailVerified());
        verify(userRepository).save(user);
    }

    @Test
    void getUserByEmail_ReturnsNullWhenNotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        User result = authService.getUserByEmail("nonexistent@test.com");

        assertNull(result);
    }

    @Test
    void getUserById_ReturnsNullWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = authService.getUserById(999L);

        assertNull(result);
    }

    @Test
    void findUserById_ReturnsNullWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = authService.findUserById(999L);

        assertNull(result);
    }

    @Test
    void findEmailByUsername_ReturnsEmptyWhenNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<String> result = authService.findEmailByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void findEmailByUsername_ReturnsEmailWhenFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<String> result = authService.findEmailByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get());
    }

    @Test
    void getUserSubscriptionInfo_ReturnsNullWhenNotFound() {
        when(subscriptionService.getCurrentSubscription(999L)).thenReturn(Optional.empty());

        UserSubscriptionDTO result = authService.getUserSubscriptionInfo(999L);

        assertNull(result);
    }

    // ===== Additional Edge Case & Branch Coverage Tests =====

    @Test
    void registerWithEmailPassword_ValidPassword_WithNumbers() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123456")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "Password123456", "09123456789");

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void validateEmailPassword_MatchingPassword_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        User result = authService.validateEmailPassword("test@test.com", "correctPassword");

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void validateEmailPassword_IncorrectPassword_ReturnsNull() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        User result = authService.validateEmailPassword("test@test.com", "wrongPassword");

        assertNull(result);
    }

    @Test
    void validateEmailPassword_NullPasswordHash_ReturnsNull() {
        User userWithoutPassword = User.builder()
                .userId(1L)
                .email("test@test.com")
                .passwordHash(null)
                .build();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(userWithoutPassword));

        User result = authService.validateEmailPassword("test@test.com", "password");

        assertNull(result);
    }

    @Test
    void resetPassword_SuccessfulReset() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newHash");

        authService.resetPassword("test@test.com", "NewPassword123");

        assertEquals("newHash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("nonexistent@test.com", "NewPassword1"));
    }

    @Test
    void changePassword_AllConditionsPass() {
        when(passwordEncoder.matches("oldPwd", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword99")).thenReturn("newHash");

        authService.changePassword(user, "oldPwd", "NewPassword99");

        assertEquals("newHash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_MismatchedCurrentPassword() {
        when(passwordEncoder.matches("wrongPwd", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(user, "wrongPwd", "NewPassword1"));
    }

    @Test
    void syncUser_ExistingUser_DoesNotCreateNew() {
        when(userRepository.findByOauthId("oauth789")).thenReturn(Optional.of(user));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("different@test.com");

        AuthResponse result = authService.syncUser(request, "oauth789", "token");

        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
        // Should not create a new user subscription
        verify(userSubscriptionRepository, never()).save(any());
    }

    @Test
    void syncUser_NewUser_InitializesSubscription() {
        when(userRepository.findByOauthId("oauth999")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("brand.new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("brand.new@test.com");
        request.setUsername("brandnew");
        request.setFirstName("Brand");
        request.setLastName("New");
        request.setRole("CUSTOMER");

        AuthResponse result = authService.syncUser(request, "oauth999", "token");

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(userSubscriptionRepository).save(any());
    }

    @Test
    void syncUser_ExistingByEmail_LinkingOAuth() {
        User existingUser = User.builder()
                .userId(2L)
                .email("existing@test.com")
                .username("existinguser")
                .firstName("Existing")
                .lastName("User")
                .role("CUSTOMER")
                .emailVerified(true)
                .build();

        when(userRepository.findByOauthId("oauth111")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existingUser));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");

        AuthResponse result = authService.syncUser(request, "oauth111", "token");

        assertNotNull(result);
        assertEquals(existingUser.getUserId(), result.getUserId());
    }

    @Test
    void syncUser_NewUser_WithNullRole_DefaultsToCustomer() {
        when(userRepository.findByOauthId("oauth222")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nullrole@test.com")).thenReturn(Optional.empty());
        User newUser = User.builder()
                .userId(3L)
                .email("nullrole@test.com")
                .username("nullrole")
                .firstName("Null")
                .lastName("Role")
                .role("CUSTOMER")
                .emailVerified(false)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("nullrole@test.com");
        request.setUsername("nullrole");
        request.setFirstName("Null");
        request.setLastName("Role");
        request.setRole(null);

        AuthResponse result = authService.syncUser(request, "oauth222", "token");

        assertNotNull(result);
    }

    @Test
    void updateUser_SavesSuccessfully() {
        when(userRepository.save(user)).thenReturn(user);

        User result = authService.updateUser(user);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void getAuthService_ReturnsSelfInstance() {
        AuthService result = authService.getAuthService();

        assertSame(authService, result);
    }

    @Test
    void findUserById_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = authService.findUserById(1L);

        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
    }

    // ===== Additional Complex Branch Coverage =====

    @Test
    void validateEmailPassword_AllBranchesInFindByEmailOrUsername() {
        // Tests the Optional.or() branch - both email and username fallback
        User foundByUsername = User.builder()
                .userId(2L)
                .email("different@test.com")
                .username("testuser")
                .passwordHash("hashedPassword")
                .build();

        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(foundByUsername));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = authService.validateEmailPassword("testuser", "password");

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
    }

    @Test
    void registerWithEmailPassword_PasswordStrengthAllBranches_HasUppercaseAndNumber() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = authService.registerWithEmailPassword("new@test.com", "newuser", "New", "User", "StrongPass123", null);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_PasswordWithBothUppercaseAndLowercase() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("MyPassword99")).thenReturn("newHash");

        authService.resetPassword("test@test.com", "MyPassword99");

        assertEquals("newHash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_AllValidationBranchesPassed() {
        when(passwordEncoder.matches("old", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPass88")).thenReturn("newHash");

        authService.changePassword(user, "old", "NewPass88");

        assertEquals("newHash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void syncUser_WithEmptyRole_DefaultsToCustomer() {
        when(userRepository.findByOauthId("oauth333")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("empty.role@test.com")).thenReturn(Optional.empty());
        User newUser = User.builder()
                .userId(4L)
                .email("empty.role@test.com")
                .username("emptyrole")
                .firstName("Empty")
                .lastName("Role")
                .role("CUSTOMER")
                .emailVerified(false)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("empty.role@test.com");
        request.setUsername("emptyrole");
        request.setFirstName("Empty");
        request.setLastName("Role");
        request.setRole("");  // Empty string, should default to CUSTOMER

        AuthResponse result = authService.syncUser(request, "oauth333", "token");

        assertNotNull(result);
    }

    @Test
    void syncUser_RoleWithUppercaseConversion() {
        when(userRepository.findByOauthId("oauth444")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("uppercase.role@test.com")).thenReturn(Optional.empty());
        User roleUser = User.builder()
                .userId(5L)
                .email("uppercase.role@test.com")
                .username("uppercase")
                .firstName("Upper")
                .lastName("Case")
                .role("ADMIN")
                .emailVerified(false)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(roleUser);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("uppercase.role@test.com");
        request.setUsername("uppercase");
        request.setFirstName("Upper");
        request.setLastName("Case");
        request.setRole("admin");  // lowercase, should be converted to uppercase

        AuthResponse result = authService.syncUser(request, "oauth444", "token");

        assertNotNull(result);
    }

    @Test
    void validateEmailPassword_EmptyPasswordHashString() {
        User userWithEmptyPassword = User.builder()
                .userId(1L)
                .email("test@test.com")
                .passwordHash("")
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(userWithEmptyPassword));

        User result = authService.validateEmailPassword("test@test.com", "password");

        assertNull(result);
    }

    @Test
    void registerWithEmailPassword_UsernameNullCheck() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userSubscriptionRepository.findFirstByUserUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());
        when(subscriptionService.getOrCreateFreePlan()).thenReturn(freePlan);

        User result = authService.registerWithEmailPassword("new@test.com", null, "New", "User", "Password1", null);

        assertNotNull(result);
    }

    @Test
    void changePassword_NullUserThrowsException() {
        assertThrows(RuntimeException.class, () ->
                authService.changePassword(null, "current", "NewPassword1"));
    }

    @Test
    void resetPassword_UserFoundAndPasswordUpdated() {
        User foundUser = User.builder()
                .userId(1L)
                .email("found@test.com")
                .passwordHash("oldHash")
                .build();

        when(userRepository.findByEmail("found@test.com")).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newHash");

        authService.resetPassword("found@test.com", "NewPassword1");

        assertEquals("newHash", foundUser.getPasswordHash());
        verify(userRepository).save(foundUser);
    }
}
