package edu.cit.sevilla.washmate.features.subscriptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.cit.sevilla.washmate.features.notifications.NotificationService;
import edu.cit.sevilla.washmate.features.payments.Payment;
import edu.cit.sevilla.washmate.features.payments.PaymentService;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;
    private Subscription freePlan;
    private Subscription proPlan;
    private UserSubscription userSubscription;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        freePlan = Subscription.builder()
                .subscriptionId(1L)
                .planType("FREE")
                .planPrice(BigDecimal.ZERO)
                .build();

        proPlan = Subscription.builder()
                .subscriptionId(2L)
                .planType("PRO")
                .planPrice(new BigDecimal("299.00"))
                .build();

        userSubscription = UserSubscription.builder()
                .userSubscriptionId(1L)
                .user(user)
                .subscription(proPlan)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusMonths(1))
                .build();
    }

    @Test
    void getOrCreateFreePlan_ReturnsExisting() {
        when(subscriptionRepository.findByPlanType("FREE")).thenReturn(Optional.of(freePlan));

        Subscription result = subscriptionService.getOrCreateFreePlan();

        assertEquals("FREE", result.getPlanType());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void getOrCreateFreePlan_CreatesNew_WhenNotFound() {
        when(subscriptionRepository.findByPlanType("FREE")).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(freePlan);

        Subscription result = subscriptionService.getOrCreateFreePlan();

        assertEquals("FREE", result.getPlanType());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void getAllPlans_Success() {
        when(subscriptionRepository.findAll()).thenReturn(Arrays.asList(freePlan, proPlan));

        List<Subscription> result = subscriptionService.getAllPlans();

        assertEquals(2, result.size());
    }

    @Test
    void initiateSubscriptionUpgrade_Success() {
        when(subscriptionRepository.findByPlanType("PRO")).thenReturn(Optional.of(proPlan));
        when(userSubscriptionRepository.findByUserUserIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
                .thenReturn(Optional.of(userSubscription));
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenReturn(userSubscription);

        UserSubscription result = subscriptionService.initiateSubscriptionUpgrade(1L, user, "PRO");

        assertNotNull(result);
        verify(paymentService).savePayment(any(Payment.class));
    }

    @Test
    void confirmSubscriptionUpgrade_Success() {
        when(userSubscriptionRepository.findById(1L)).thenReturn(Optional.of(userSubscription));
        when(paymentService.getPaymentByPaymongoIntentId("pay_123")).thenReturn(Optional.empty());
        when(paymentService.savePayment(any(Payment.class))).thenReturn(new Payment());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenReturn(userSubscription);

        UserSubscription result = subscriptionService.confirmSubscriptionUpgrade(1L, "pay_123", "GCASH");

        assertEquals("ACTIVE", result.getStatus());
        assertEquals("pay_123", result.getPaymongoPaymentId());
        verify(notificationService).notifySubscriptionUpgrade(1L, "PRO");
    }

    @Test
    void getCurrentSubscription_Success() {
        when(userSubscriptionRepository.findByUserUserIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
                .thenReturn(Optional.of(userSubscription));

        Optional<UserSubscription> result = subscriptionService.getCurrentSubscription(1L);

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());
    }

    @Test
    void getSubscriptionHistory_Success() {
        when(userSubscriptionRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(userSubscription));

        List<UserSubscription> result = subscriptionService.getSubscriptionHistory(1L);

        assertEquals(1, result.size());
    }

    @Test
    void initializeUsersWithFreeSubscription_Success() {
        when(subscriptionRepository.findByPlanType("FREE")).thenReturn(Optional.of(freePlan));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        when(userSubscriptionRepository.findByUserUserIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
                .thenReturn(Optional.empty());

        int count = subscriptionService.initializeUsersWithFreeSubscription();

        assertEquals(1, count);
        verify(userSubscriptionRepository).save(any(UserSubscription.class));
    }

    @Test
    void updatePlanPrice_Success() {
        when(subscriptionRepository.findById(2L)).thenReturn(Optional.of(proPlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(proPlan);

        Subscription result = subscriptionService.updatePlanPrice(2L, new BigDecimal("399.00"));

        assertEquals(new BigDecimal("399.00"), proPlan.getPlanPrice());
    }
}
