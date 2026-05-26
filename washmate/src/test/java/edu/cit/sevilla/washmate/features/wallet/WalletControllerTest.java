package edu.cit.sevilla.washmate.features.wallet;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import edu.cit.sevilla.washmate.features.payments.PayMongoService;
import edu.cit.sevilla.washmate.features.payments.PaymentService;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import edu.cit.sevilla.washmate.WashmateApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WashmateApplication.class)
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PayMongoService payMongoService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        wallet = Wallet.builder()
                .walletId(1L)
                .user(user)
                .availableBalance(new BigDecimal("500.00"))
                .currency("PHP")
                .build();
    }

    @Test
    void getWalletBalance_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.getOrCreateWallet(1L)).thenReturn(wallet);
        
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setWalletId(1L);
        walletDTO.setAvailableBalance(new BigDecimal("500.00"));
        walletDTO.setCurrency("PHP");
        
        when(walletService.toWalletDTO(wallet)).thenReturn(walletDTO);

        mockMvc.perform(get("/api/wallet/balance")
                .with(jwt().jwt(jwt -> jwt.subject("1"))) // Mock JWT with sub="1"
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(500.00))
                .andExpect(jsonPath("$.currency").value("PHP"));
    }

    @Test
    void checkSufficientBalance_ReturnsTrue_WhenBalanceIsEnough() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.hasSufficientBalance(1L, new BigDecimal("100.00"))).thenReturn(true);
        when(walletService.getWalletBalance(1L)).thenReturn(new BigDecimal("500.00"));

        mockMvc.perform(get("/api/wallet/balance/check")
                .param("amount", "100.00")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSufficientBalance").value(true))
                .andExpect(jsonPath("$.currentBalance").value(500.00));
    }

    @Test
    void initiateWalletTopup_NegativeAmount_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/wallet/topup/initiate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\":-100.00,\"paymentMethod\":\"CARD\"}")
                    .with(jwt().jwt(jwt -> jwt.subject("1"))));
        });
    }

    @Test
    void checkSufficientBalance_ReturnsFalse_WhenBalanceIsNotEnough() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.hasSufficientBalance(1L, new BigDecimal("1000.00"))).thenReturn(false);
        when(walletService.getWalletBalance(1L)).thenReturn(new BigDecimal("500.00"));

        mockMvc.perform(get("/api/wallet/balance/check")
                .param("amount", "1000.00")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSufficientBalance").value(false))
                .andExpect(jsonPath("$.currentBalance").value(500.00));
    }

    @Test
    void getWalletTransactions_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.getWalletTransactions(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/wallet/transactions")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void addToWallet_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.addToWallet(eq(1L), any(BigDecimal.class))).thenReturn(wallet);
        when(walletService.toWalletDTO(any())).thenReturn(new WalletDTO());

        mockMvc.perform(post("/api/wallet/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void initiateWalletTopup_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        payment.setReferenceType("WALLET_TOPUP");
        payment.setReferenceId(1L);
        when(walletService.initiateWalletTopup(eq(1L), any(BigDecimal.class), any(String.class))).thenReturn(payment);

        mockMvc.perform(post("/api/wallet/topup/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00,\"paymentMethod\":\"CARD\"}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void processWalletTopupPayment_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        when(walletService.initiateWalletTopup(eq(1L), any(BigDecimal.class), any(String.class))).thenReturn(payment);
        
        when(payMongoService.createCheckoutSession(any(BigDecimal.class), any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("PayMongo error"));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/wallet/topup/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\":100.00,\"paymentMethod\":\"CARD\"}")
                    .with(jwt().jwt(jwt -> jwt.subject("1"))));
        });
    }

    @Test
    void getPendingTransactions_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.getPendingTransactions(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/wallet/transactions/pending")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void processWalletTopupPayment_Success_Card() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        when(walletService.initiateWalletTopup(eq(1L), any(BigDecimal.class), any(String.class))).thenReturn(payment);
        
        Map<String, String> sessionResult = new HashMap<>();
        sessionResult.put("checkoutUrl", "http://checkout.url");
        when(payMongoService.createCheckoutSession(any(BigDecimal.class), any(String.class), any(String.class))).thenReturn(sessionResult);

        mockMvc.perform(post("/api/wallet/topup/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00,\"paymentMethod\":\"CARD\"}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout.url"));
    }

    @Test
    void processWalletTopupPayment_Success_GCash() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        when(walletService.initiateWalletTopup(eq(1L), any(BigDecimal.class), any(String.class))).thenReturn(payment);
        
        Map<String, String> sourceResult = new HashMap<>();
        sourceResult.put("checkoutUrl", "http://checkout.url");
        sourceResult.put("sourceId", "src_123");
        when(payMongoService.createSource(any(String.class), any(BigDecimal.class), any(String.class), any(String.class))).thenReturn(sourceResult);

        mockMvc.perform(post("/api/wallet/topup/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00,\"paymentMethod\":\"GCASH\"}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout.url"));
    }

    @Test
    void processWalletTopupPayment_Success_PayMaya() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        when(walletService.initiateWalletTopup(eq(1L), any(BigDecimal.class), any(String.class))).thenReturn(payment);
        
        Map<String, String> sourceResult = new HashMap<>();
        sourceResult.put("checkoutUrl", "http://checkout.url");
        sourceResult.put("sourceId", "src_123");
        when(payMongoService.createSource(eq("paymaya"), any(BigDecimal.class), any(String.class), any(String.class))).thenReturn(sourceResult);

        mockMvc.perform(post("/api/wallet/topup/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00,\"paymentMethod\":\"PAYMAYA\"}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout.url"));
    }

    @Test
    void confirmWalletTopup_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.confirmWalletTopup(eq(1L), eq("1"), any(BigDecimal.class), any())).thenReturn(new WalletTransaction());
        when(walletService.getOrCreateWallet(1L)).thenReturn(wallet);
        when(walletService.toWalletDTO(any())).thenReturn(new WalletDTO());

        mockMvc.perform(post("/api/wallet/topup/confirm/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void confirmWalletTopup_WithIntentId_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.confirmWalletTopup(eq(1L), eq("1"), any(BigDecimal.class), eq("pi_123"))).thenReturn(new WalletTransaction());
        when(walletService.getOrCreateWallet(1L)).thenReturn(wallet);
        when(walletService.toWalletDTO(any())).thenReturn(new WalletDTO());

        mockMvc.perform(post("/api/wallet/topup/confirm/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00,\"paymongoPaymentIntentId\":\"pi_123\"}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactionsByType_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.getTransactionsByReferenceType(1L, "ORDER_PAYMENT")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/wallet/transactions/type/ORDER_PAYMENT")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void deductFromWallet_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.deductFromWallet(eq(1L), any(BigDecimal.class), any(String.class), any())).thenReturn(wallet);
        when(walletService.getOrCreateWallet(1L)).thenReturn(wallet);
        when(walletService.toWalletDTO(any())).thenReturn(new WalletDTO());

        mockMvc.perform(post("/api/wallet/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":50.00,\"referenceType\":\"MANUAL\",\"referenceId\":null}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void initiateWalletTopup_NullAmount_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/wallet/topup/initiate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"paymentMethod\":\"CARD\"}")
                    .with(jwt().jwt(jwt -> jwt.subject("1"))));
        });
    }

    @Test
    void initiateWalletTopup_DefaultMethod_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        when(walletService.initiateWalletTopup(eq(1L), any(BigDecimal.class), eq("CARD"))).thenReturn(payment);

        mockMvc.perform(post("/api/wallet/topup/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void processWalletTopupPayment_UnsupportedMethod_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/wallet/topup/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\":100.00,\"paymentMethod\":\"INVALID\"}")
                    .with(jwt().jwt(jwt -> jwt.subject("1"))));
        });
    }

    @Test
    void confirmWalletTopup_NoAmount_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.confirmWalletTopup(eq(1L), eq("1"), eq(null), any())).thenReturn(new WalletTransaction());
        when(walletService.getOrCreateWallet(1L)).thenReturn(wallet);
        when(walletService.toWalletDTO(any())).thenReturn(new WalletDTO());

        mockMvc.perform(post("/api/wallet/topup/confirm/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void addToWallet_NullAmount_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/wallet/credit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .with(jwt().jwt(jwt -> jwt.subject("1"))));
        });
    }

    @Test
    void addToWallet_NegativeAmount_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/wallet/credit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\":-100.00}")
                    .with(jwt().jwt(jwt -> jwt.subject("1"))));
        });
    }

    @Test
    void deductFromWallet_NegativeAmount_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/wallet/debit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\":-50.00,\"referenceType\":\"MANUAL\",\"referenceId\":null}")
                    .with(jwt().jwt(jwt -> jwt.subject("1"))));
        });
    }

    @Test
    void confirmWalletTopup_NoIntentId_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletService.confirmWalletTopup(eq(1L), eq("1"), any(BigDecimal.class), eq(null))).thenReturn(new WalletTransaction());
        when(walletService.getOrCreateWallet(1L)).thenReturn(wallet);
        when(walletService.toWalletDTO(any())).thenReturn(new WalletDTO());

        mockMvc.perform(post("/api/wallet/topup/confirm/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00}")
                .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());
    }
}
