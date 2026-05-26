package edu.cit.sevilla.washmate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.subscriptions.SubscriptionRepository;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscription;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import edu.cit.sevilla.washmate.features.users.User;

@ExtendWith(MockitoExtension.class)
class SubscriptionRenewalServiceTest {

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionRenewalService subscriptionRenewalService;

    private UserSubscription freeSub;
    private UserSubscription premiumSub;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        Subscription freePlan = new Subscription();
        freePlan.setPlanPrice(BigDecimal.ZERO);

        Subscription premiumPlan = new Subscription();
        premiumPlan.setPlanPrice(new BigDecimal("100.00"));

        freeSub = new UserSubscription();
        freeSub.setUser(user);
        freeSub.setSubscription(freePlan);
        freeSub.setStatus("ACTIVE");
        freeSub.setExpiryDate(LocalDateTime.now().minusDays(1));

        premiumSub = new UserSubscription();
        premiumSub.setUser(user);
        premiumSub.setSubscription(premiumPlan);
        premiumSub.setStatus("ACTIVE");
        premiumSub.setExpiryDate(LocalDateTime.now().minusDays(1));
    }

    @Test
    void processRenewals_NoExpiredSubs() {
        when(userSubscriptionRepository.findByStatusAndExpiryDateBefore(eq("ACTIVE"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        subscriptionRenewalService.processRenewals();

        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
    }

    @Test
    void processRenewals_FreePlan_AutoRenews() {
        when(userSubscriptionRepository.findByStatusAndExpiryDateBefore(eq("ACTIVE"), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(freeSub));

        subscriptionRenewalService.processRenewals();

        verify(userSubscriptionRepository).save(freeSub);
        assertEquals("ACTIVE", freeSub.getStatus());
        assertTrue(freeSub.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void processRenewals_PremiumPlan_Expires() {
        when(userSubscriptionRepository.findByStatusAndExpiryDateBefore(eq("ACTIVE"), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(premiumSub));

        subscriptionRenewalService.processRenewals();

        verify(userSubscriptionRepository).save(premiumSub);
        assertEquals("EXPIRED", premiumSub.getStatus());
    }

    @Test
    void processRenewals_Exception_Logged() {
        when(userSubscriptionRepository.findByStatusAndExpiryDateBefore(eq("ACTIVE"), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("DB Error"));

        assertDoesNotThrow(() -> subscriptionRenewalService.processRenewals());
    }
}
