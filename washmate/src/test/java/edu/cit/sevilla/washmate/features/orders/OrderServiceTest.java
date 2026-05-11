package edu.cit.sevilla.washmate.features.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.cit.sevilla.washmate.features.notifications.NotificationService;
import edu.cit.sevilla.washmate.features.payments.Payment;
import edu.cit.sevilla.washmate.features.payments.PaymentService;
import edu.cit.sevilla.washmate.features.services.ServiceVariant;
import edu.cit.sevilla.washmate.features.services.ServiceVariantRepository;
import edu.cit.sevilla.washmate.features.services.WashService;
import edu.cit.sevilla.washmate.features.services.WashServiceRepository;
import edu.cit.sevilla.washmate.features.users.AddressRepository;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WashServiceRepository washServiceRepository;
    @Mock
    private ServiceVariantRepository serviceVariantRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private Order order;
    private WashService washService;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setUserId(1L);
        customer.setUsername("customer");

        order = Order.builder()
                .orderId(1L)
                .customer(customer)
                .orderNumber("WM-12345")
                .status("PENDING")
                .totalAmount(new BigDecimal("100.00"))
                .orderServices(new ArrayList<>())
                .build();

        washService = WashService.builder()
                .serviceId(1L)
                .serviceName("Full Wash")
                .basePricePerUnit(new BigDecimal("100.00"))
                .build();

        OrderServiceInput input = new OrderServiceInput();
        input.setServiceId(1L);
        input.setQuantity(new BigDecimal("1"));

        orderRequest = new OrderRequest();
        orderRequest.setServices(Arrays.asList(input));
    }

    @Test
    void createOrder_Success() {
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
        verify(entityManager).flush();
        verify(entityManager).refresh(order);
    }

    @Test
    void createOrder_ThrowsException_WhenNoServices() {
        orderRequest.setServices(new ArrayList<>());

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(orderRequest, customer);
        });
    }

    @Test
    void initiateOrderPayment_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.createAndSaveOrderPayment(anyLong(), any(BigDecimal.class), anyString()))
                .thenReturn(new Payment());

        Payment result = orderService.initiateOrderPayment(1L, "GCASH");

        assertNotNull(result);
    }

    @Test
    void initiateOrderPayment_ThrowsException_WhenNotPending() {
        order.setStatus("CONFIRMED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> {
            orderService.initiateOrderPayment(1L, "GCASH");
        });
    }

    @Test
    void confirmOrderPayment_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.getPaymentByPaymongoIntentId("pay_123")).thenReturn(Optional.empty());
        when(paymentService.savePayment(any(Payment.class))).thenReturn(new Payment());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.confirmOrderPayment(1L, "pay_123", "GCASH");

        assertEquals("CONFIRMED", result.getStatus());
        verify(notificationService).notifyPaymentSuccess(anyLong(), anyLong(), anyString());
    }

    @Test
    void updateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void updateOrderStatus_ThrowsException_WhenCancellingNonPending() {
        order.setStatus("CONFIRMED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> {
            orderService.updateOrderStatus(1L, "CANCELLED");
        });
    }
}
