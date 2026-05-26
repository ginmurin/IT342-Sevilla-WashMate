package edu.cit.sevilla.washmate.features.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.List;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import edu.cit.sevilla.washmate.WashmateApplication;
import edu.cit.sevilla.washmate.features.payments.PayMongoService;
import edu.cit.sevilla.washmate.features.payments.PaymentService;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import edu.cit.sevilla.washmate.features.wallet.WalletService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WashmateApplication.class)
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PayMongoService payMongoService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private WalletService walletService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setRole("CUSTOMER");

        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setCustomer(testUser);

        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        lenient().when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    }

    @Test
    void getOrderById_Success() throws Exception {
        when(orderService.toOrderDTO(any(Order.class))).thenReturn(new OrderDTO());

        mockMvc.perform(get("/api/orders/1")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getMyOrders_Success() throws Exception {
        when(orderService.getOrdersByCustomer(1L)).thenReturn(Collections.emptyList());
        when(orderService.toOrderDTOs(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/my-orders")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_Success() throws Exception {
        when(orderService.updateOrderStatus(eq(1L), eq("CANCELLED"))).thenReturn(testOrder);
        when(orderService.toOrderDTO(any(Order.class))).thenReturn(new OrderDTO());

        mockMvc.perform(post("/api/orders/1/cancel")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder_Success() throws Exception {
        OrderRequest request = new OrderRequest();
        when(orderService.createOrder(any(OrderRequest.class), any(User.class))).thenReturn(testOrder);
        when(orderService.toOrderDTO(any(Order.class))).thenReturn(new OrderDTO());

        mockMvc.perform(post("/api/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"services\":[{\"serviceId\":1,\"quantity\":1.0}]}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void initiateOrderPayment_Success() throws Exception {
        when(orderService.initiateOrderPayment(eq(1L), eq("CARD")))
                .thenReturn(new edu.cit.sevilla.washmate.features.payments.Payment());
        when(orderService.toPaymentDTO(any())).thenReturn(new edu.cit.sevilla.washmate.features.payments.PaymentDTO());

        mockMvc.perform(post("/api/orders/1/payment/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderPayments_Success() throws Exception {
        when(orderService.getOrderPayments(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/1/payments")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getOrdersByStatus_Success() throws Exception {
        when(orderService.getOrdersByStatus("PENDING")).thenReturn(Collections.emptyList());
        when(orderService.toOrderDTOs(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/status/PENDING")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getOrdersByStatus_Filter_Success() throws Exception {
        User user = new User();
        user.setUserId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Order order1 = new Order();
        order1.setCustomer(user);
        
        User otherUser = new User();
        otherUser.setUserId(2L);
        Order order2 = new Order();
        order2.setCustomer(otherUser);

        when(orderService.getOrdersByStatus("PENDING")).thenReturn(java.util.List.of(order1, order2));

        mockMvc.perform(get("/api/orders/status/PENDING")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_AccessDenied_Failure() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setRole("CUSTOMER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User otherUser = new User();
        otherUser.setUserId(2L);
        Order order = new Order();
        order.setCustomer(otherUser);
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));

        assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(get("/api/orders/1")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void updateOrderStatus_Success() throws Exception {
        when(orderService.updateOrderStatus(eq(1L), eq("COMPLETED"))).thenReturn(testOrder);
        when(orderService.toOrderDTO(any(Order.class))).thenReturn(new OrderDTO());

        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_Success() throws Exception {
        User admin = new User();
        admin.setUserId(2L);
        admin.setRole("ADMIN");
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/all")
                .with(jwt().jwt(builder -> builder.subject("2"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_Forbidden() throws Exception {
        User customer = new User();
        customer.setUserId(1L);
        customer.setRole("CUSTOMER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/orders/all")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrders_ShopOwner_Success() throws Exception {
        User shopOwner = new User();
        shopOwner.setUserId(3L);
        shopOwner.setRole("SHOP_OWNER");
        when(userRepository.findById(3L)).thenReturn(Optional.of(shopOwner));
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/all")
                .with(jwt().jwt(builder -> builder.subject("3"))))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder_UserNotFound_Failure() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/orders/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"services\":[{\"serviceId\":1,\"quantity\":1.0}]}")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void processOrderPayment_OrderNotFound_Failure() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(Optional.empty());

        assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/orders/1/payment/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"paymentMethod\":\"CARD\"}")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void initiateOrderPayment_DefaultMethod_Success() throws Exception {
        when(orderService.initiateOrderPayment(eq(1L), eq("CARD")))
                .thenReturn(new edu.cit.sevilla.washmate.features.payments.Payment());

        mockMvc.perform(post("/api/orders/1/payment/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }

    @Test
    void processOrderPayment_Card_Success() throws Exception {
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        testOrder.setTotalAmount(new java.math.BigDecimal("100.00"));

        when(orderService.initiateOrderPayment(eq(1L), eq("CARD"))).thenReturn(payment);
        when(payMongoService.createCheckoutSession(any(java.math.BigDecimal.class), any(), any()))
                .thenReturn(java.util.Map.of("checkoutUrl", "http://checkout", "sessionId", "sess_123"));

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout"));
    }

    @Test
    void processOrderPayment_Wallet_Success() throws Exception {
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        testOrder.setTotalAmount(new java.math.BigDecimal("100.00"));

        when(orderService.initiateOrderPayment(eq(1L), eq("WALLET"))).thenReturn(payment);

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"WALLET\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletPayment").value(true));
    }

    @Test
    void processOrderPayment_GCash_Success() throws Exception {
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        testOrder.setTotalAmount(new java.math.BigDecimal("100.00"));

        when(orderService.initiateOrderPayment(eq(1L), eq("GCASH"))).thenReturn(payment);
        when(payMongoService.createSource(eq("gcash"), any(java.math.BigDecimal.class), any(), any()))
                .thenReturn(java.util.Map.of("checkoutUrl", "http://checkout", "sourceId", "src_123"));

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"GCASH\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout"));
    }

    @Test
    void processOrderPayment_Paymaya_Success() throws Exception {
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        testOrder.setTotalAmount(new java.math.BigDecimal("100.00"));

        when(orderService.initiateOrderPayment(eq(1L), eq("PAYMAYA"))).thenReturn(payment);
        when(payMongoService.createSource(eq("maya"), any(java.math.BigDecimal.class), any(), any()))
                .thenReturn(java.util.Map.of("checkoutUrl", "http://checkout", "sourceId", "src_123"));

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"PAYMAYA\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout"));
    }

    @Test
    void processOrderPayment_Failure() throws Exception {
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        testOrder.setTotalAmount(new java.math.BigDecimal("100.00"));

        when(orderService.initiateOrderPayment(eq(1L), eq("CARD"))).thenReturn(payment);
        when(payMongoService.createCheckoutSession(any(java.math.BigDecimal.class), any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("PayMongo error"));

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PayMongo error"));
    }

    @Test
    void processOrderPayment_GrabPay_Success() throws Exception {
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);
        testOrder.setTotalAmount(new java.math.BigDecimal("100.00"));

        when(orderService.initiateOrderPayment(eq(1L), eq("GRAB_PAY"))).thenReturn(payment);
        when(payMongoService.createSource(eq("grab_pay"), any(java.math.BigDecimal.class), any(), any()))
                .thenReturn(java.util.Map.of("checkoutUrl", "http://checkout", "sourceId", "src_123"));

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"GRAB_PAY\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://checkout"));
    }

    @Test
    void processOrderPayment_InvalidMethod_Failure() throws Exception {
        edu.cit.sevilla.washmate.features.payments.Payment payment = new edu.cit.sevilla.washmate.features.payments.Payment();
        payment.setPaymentId(1L);

        when(orderService.initiateOrderPayment(eq(1L), eq("INVALID"))).thenReturn(payment);

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"INVALID\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void processOrderPayment_InitiateFailure() throws Exception {
        when(orderService.initiateOrderPayment(eq(1L), eq("CARD"))).thenThrow(new RuntimeException("Initiate error"));

        mockMvc.perform(post("/api/orders/1/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Initiate error"));
    }

    @Test
    void updateOrderStatus_NullStatus_Failure() throws Exception {
        assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(put("/api/orders/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void updateOrderStatus_EmptyStatus_Failure() throws Exception {
        assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(put("/api/orders/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\" \"}")
                    .with(jwt().jwt(builder -> builder.subject("1"))));
        });
    }

    @Test
    void confirmOrderPayment_Success() throws Exception {
        when(orderService.confirmOrderPayment(eq(1L), eq("1"), eq("CARD"))).thenReturn(testOrder);
        when(orderService.toOrderDTO(any(Order.class))).thenReturn(new OrderDTO());

        mockMvc.perform(post("/api/orders/1/payment/confirm/1")
                .param("paymentMethod", "CARD")
                .with(jwt().jwt(builder -> builder.subject("1"))))
                .andExpect(status().isOk());
    }
}
