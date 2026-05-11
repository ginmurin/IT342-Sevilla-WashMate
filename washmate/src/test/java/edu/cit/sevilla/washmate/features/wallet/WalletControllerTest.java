package edu.cit.sevilla.washmate.features.wallet;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.math.BigDecimal;
import java.util.Optional;

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

@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
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
}
