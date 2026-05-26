package edu.cit.sevilla.washmate.features.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.sevilla.washmate.features.orders.Order;
import edu.cit.sevilla.washmate.features.orders.OrderRepository;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscription;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import edu.cit.sevilla.washmate.features.wallet.WalletTransaction;
import edu.cit.sevilla.washmate.features.wallet.WalletTransactionRepository;
import edu.cit.sevilla.washmate.features.wallet.WalletService;
import edu.cit.sevilla.washmate.features.wallet.Wallet;
import edu.cit.sevilla.washmate.features.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PayMongoService payMongoService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private WalletService walletService;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private UserSubscriptionRepository userSubscriptionRepository;

    @MockBean
    private WalletTransactionRepository walletTransactionRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void handlePayMongoWebhook_Success_Order() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment_intent.succeeded");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        attributes.put("status", "succeeded");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReferenceType("ORDER");
        payment.setReferenceId(1L);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload)
                .header("X-Paymongo-Signature", "valid_signature"))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_Failure_SignatureInvalid() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", new HashMap<>());
        String rawPayload = objectMapper.writeValueAsString(payload);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload)
                .header("X-Paymongo-Signature", "invalid_signature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void handlePayMongoWebhook_Success_Subscription() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment_intent.succeeded");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        attributes.put("status", "succeeded");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReferenceType("SUBSCRIPTION");
        payment.setReferenceId(1L);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));
        when(userSubscriptionRepository.findById(1L)).thenReturn(Optional.of(new UserSubscription()));

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_Failure_PaymentFailed() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment_intent.failed");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_Success_WalletTopup() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment_intent.succeeded");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        attributes.put("status", "succeeded");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReferenceType("WALLET_TOPUP");
        payment.setReferenceId(1L);

        WalletTransaction txn = new WalletTransaction();
        txn.setAmount(new java.math.BigDecimal("100.00"));
        Wallet wallet = new Wallet();
        User user = new User();
        user.setUserId(1L);
        wallet.setUser(user);
        txn.setWallet(wallet);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));
        when(walletTransactionRepository.findById(1L)).thenReturn(Optional.of(txn));

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_SourceChargeable() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "source.chargeable");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "src_123");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_BadRequest_NoData() throws Exception {
        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handlePayMongoWebhook_Success_PaymentSuccessEvent() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment.success");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReferenceType("ORDER");
        payment.setReferenceId(1L);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_Failure_PaymentFailedEvent() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment.failed");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_UnknownEvent() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "unknown.event");
        Map<String, Object> attributes = new HashMap<>();
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_PaymentNotFound() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment_intent.succeeded");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_OrderNotFound() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment_intent.succeeded");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReferenceType("ORDER");
        payment.setReferenceId(1L);

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }

    @Test
    void handlePayMongoWebhook_UnknownReferenceType() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "payment_intent.succeeded");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "pi_123");
        data.put("attributes", attributes);
        payload.put("data", data);

        String rawPayload = objectMapper.writeValueAsString(payload);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReferenceType("UNKNOWN");

        when(payMongoService.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymongoPaymentIntentId("pi_123")).thenReturn(Optional.of(payment));

        mockMvc.perform(post("/api/webhook/paymongo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
                .andExpect(status().isOk());
    }
}
