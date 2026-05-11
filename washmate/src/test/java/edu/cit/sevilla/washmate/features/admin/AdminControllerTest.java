package edu.cit.sevilla.washmate.features.admin;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.cit.sevilla.washmate.features.orders.Order;
import edu.cit.sevilla.washmate.features.orders.OrderRepository;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import edu.cit.sevilla.washmate.features.wallet.Wallet;
import edu.cit.sevilla.washmate.features.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User regularUser;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");

        regularUser = new User();
        regularUser.setUserId(2L);
        regularUser.setUsername("user");
        regularUser.setRole("CUSTOMER");

        wallet = new Wallet();
        wallet.setUser(regularUser);
        wallet.setAvailableBalance(new BigDecimal("100.00"));
    }

    @Test
    void getGlobalStats_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.count()).thenReturn(10L);
        when(orderRepository.count()).thenReturn(5L);
        
        Order order = new Order();
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setStatus("COMPLETED");
        
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));
        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, regularUser));

        mockMvc.perform(get("/api/admin/stats")
                .with(jwt().jwt(jwt -> jwt.subject("1"))) // Admin ID is 1
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.totalRevenue").value(500.00));
    }

    @Test
    void getGlobalStats_AccessDenied_WhenNotAdmin() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(get("/api/admin/stats")
                    .with(jwt().jwt(jwt -> jwt.subject("2"))) // Regular user ID is 2
                    .contentType(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    void getAllUsers_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, regularUser));
        when(walletRepository.findByUserUserId(2L)).thenReturn(Optional.of(wallet));

        mockMvc.perform(get("/api/admin/users")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].username").value("user"))
                .andExpect(jsonPath("$[1].walletBalance").value(100.00));
    }

    @Test
    void updateUserRole_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        Map<String, String> request = new HashMap<>();
        request.put("role", "SHOP_OWNER");

        mockMvc.perform(put("/api/admin/users/2/role")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
                
        verify(userRepository).save(regularUser);
    }

    @Test
    void updateUserStatus_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        Map<String, String> request = new HashMap<>();
        request.put("status", "INACTIVE");

        mockMvc.perform(put("/api/admin/users/2/status")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
                
        verify(userRepository).save(regularUser);
    }

    @Test
    void deleteUser_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        mockMvc.perform(delete("/api/admin/users/2")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
                
        verify(userRepository).delete(regularUser);
    }

    @Test
    void deleteUser_CannotDeleteSelf() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(delete("/api/admin/users/1")
                    .with(jwt().jwt(jwt -> jwt.subject("1")))
                    .contentType(MediaType.APPLICATION_JSON));
        });
    }

    @Test
    void updateUserWallet_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(walletRepository.findByUserUserId(2L)).thenReturn(Optional.of(wallet));

        Map<String, Double> request = new HashMap<>();
        request.put("balance", 500.00);

        mockMvc.perform(put("/api/admin/users/2/wallet")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
                
        verify(walletRepository).save(wallet);
    }
}
