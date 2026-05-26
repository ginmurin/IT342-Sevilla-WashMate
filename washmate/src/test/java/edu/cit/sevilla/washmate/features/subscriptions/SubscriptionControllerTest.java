package edu.cit.sevilla.washmate.features.subscriptions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.Optional;
import java.util.Collections;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import edu.cit.sevilla.washmate.WashmateApplication;
import edu.cit.sevilla.washmate.features.payments.PayMongoService;
import edu.cit.sevilla.washmate.features.payments.PaymentService;
import edu.cit.sevilla.washmate.features.payments.Payment;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WashmateApplication.class)
@AutoConfigureMockMvc
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PayMongoService payMongoService;

    @MockBean
    private PaymentService paymentService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setRole("CUSTOMER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    @Test
    void getMySubscription_NotFound() throws Exception {
        when(subscriptionService.getCurrentSubscription(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/subscriptions/me")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAvailablePlans_Success() throws Exception {
        when(subscriptionService.getAllPlans()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/subscriptions/plans")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getMySubscription_Success() throws Exception {
        User user = new User();
        user.setUserId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionService.getCurrentSubscription(1L)).thenReturn(Optional.of(new UserSubscription()));
        when(subscriptionService.toUserSubscriptionDTO(any())).thenReturn(new UserSubscriptionDTO());

        mockMvc.perform(get("/api/subscriptions/me")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getSubscriptionHistory_Success() throws Exception {
        when(subscriptionService.getSubscriptionHistory(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/subscriptions/history")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void initiateUpgrade_Success() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);
        userSub.setExpiryDate(java.time.LocalDateTime.now().plusDays(30));

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userSubscriptionId").value(1));
    }

    @Test
    void processUpgradePayment_Card_Success() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(Collections.singletonList(payment));
        when(payMongoService.createPaymentIntent(any(BigDecimal.class))).thenReturn(Map.of("paymentIntentId", "pi_123", "clientKey", "ck_123"));

        mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentIntentId").value("pi_123"));
    }

    @Test
    void processUpgradePayment_Wallet_Success() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(Collections.singletonList(payment));

        mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"WALLET\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletPayment").value(true));
    }

    @Test
    void processUpgradePayment_GCash_Success() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(Collections.singletonList(payment));
        when(payMongoService.createSource(eq("gcash"), any(BigDecimal.class), any(String.class), any(String.class)))
                .thenReturn(Map.of("checkoutUrl", "http://checkout", "sourceId", "src_123"));

        mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"GCASH\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout"));
    }

    @Test
    void processUpgradePayment_Failure() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(Collections.singletonList(payment));
        when(payMongoService.createPaymentIntent(any(BigDecimal.class)))
                .thenThrow(new RuntimeException("PayMongo error"));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"paymentMethod\":\"CARD\"}")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void processUpgradePayment_NoPayment_Failure() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(Collections.emptyList());

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"paymentMethod\":\"CARD\"}")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void confirmUpgrade_Success() throws Exception {
        when(subscriptionService.confirmSubscriptionUpgrade(eq(1L), eq("1"), eq("CARD")))
                .thenReturn(new UserSubscription());
        when(subscriptionService.toUserSubscriptionDTO(any())).thenReturn(new UserSubscriptionDTO());

        mockMvc.perform(post("/api/subscriptions/confirm-upgrade/1/1")
                .param("paymentMethod", "CARD")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void confirmUpgrade_Failure() throws Exception {
        when(subscriptionService.confirmSubscriptionUpgrade(eq(1L), eq("1"), eq("CARD")))
                .thenThrow(new RuntimeException("Service error"));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/subscriptions/confirm-upgrade/1/1")
                    .param("paymentMethod", "CARD")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void initiateUpgrade_WithPaymentFound() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);
        userSub.setExpiryDate(java.time.LocalDateTime.now().plusDays(30));

        Payment payment = new Payment();
        payment.setPaymentId(99L);

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(java.util.List.of(payment));

        mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(99));
    }

    @Test
    void processUpgradePayment_UnsupportedMethod() throws Exception {
        UserSubscription userSub = new UserSubscription();
        userSub.setUserSubscriptionId(1L);
        Subscription plan = new Subscription();
        plan.setPlanType("PREMIUM");
        plan.setPlanPrice(new BigDecimal("100.00"));
        userSub.setSubscription(plan);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(subscriptionService.initiateSubscriptionUpgrade(eq(1L), any(User.class), eq("PREMIUM")))
                .thenReturn(userSub);
        when(subscriptionService.getSubscriptionPayments(1L)).thenReturn(Collections.singletonList(payment));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/subscriptions/upgrade/PREMIUM/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"paymentMethod\":\"BITCOIN\"}")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void initializeFreeUsers_Success() throws Exception {
        when(subscriptionService.initializeUsersWithFreeSubscription()).thenReturn(5);

        mockMvc.perform(post("/api/subscriptions/initialize-free-users")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initializedUsersCount").value(5));
    }

    @Test
    void updatePlanPrice_Success() throws Exception {
        Subscription updated = new Subscription();
        updated.setSubscriptionId(1L);
        updated.setPlanType("PREMIUM");
        updated.setPlanPrice(new BigDecimal("200.00"));

        when(subscriptionService.updatePlanPrice(eq(1L), any(BigDecimal.class))).thenReturn(updated);
        when(subscriptionService.getSubscriptionDTO(any(Subscription.class)))
                .thenReturn(Optional.of(new SubscriptionDTO()));

        mockMvc.perform(put("/api/subscriptions/plans/1/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"planPrice\":200.00}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }
}
