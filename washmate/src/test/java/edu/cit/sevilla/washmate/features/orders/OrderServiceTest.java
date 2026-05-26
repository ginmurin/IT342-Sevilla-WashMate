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
import edu.cit.sevilla.washmate.features.payments.PaymentDTO;
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

    // ===== Additional Comprehensive Tests for Branch Coverage =====

    @Test
    void getOrderById_Found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getOrderId());
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getOrdersByCustomer_Success() {
        when(orderRepository.findByCustomerUserId(1L)).thenReturn(Arrays.asList(order));

        List<Order> result = orderService.getOrdersByCustomer(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByCustomer_Empty() {
        when(orderRepository.findByCustomerUserId(999L)).thenReturn(Arrays.asList());

        List<Order> result = orderService.getOrdersByCustomer(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getOrdersByStatus_Success() {
        when(orderRepository.findByStatus("CONFIRMED")).thenReturn(Arrays.asList(order));

        List<Order> result = orderService.getOrdersByStatus("CONFIRMED");

        assertEquals(1, result.size());
    }

    @Test
    void initiateOrderPayment_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.initiateOrderPayment(999L, "GCASH"));
    }

    @Test
    void confirmOrderPayment_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrderPayment(999L, "pay_123", "GCASH"));
    }

    @Test
    void updateOrderStatus_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrderStatus(999L, "COMPLETED"));
    }

    @Test
    void createOrder_WithMultipleServices() {
        OrderServiceInput input2 = new OrderServiceInput();
        input2.setServiceId(2L);
        input2.setQuantity(new BigDecimal("2"));
        orderRequest.setServices(Arrays.asList(
                orderRequest.getServices().get(0),
                input2));

        WashService service2 = WashService.builder()
                .serviceId(2L)
                .serviceName("Express Wash")
                .basePricePerUnit(new BigDecimal("150.00"))
                .build();

        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(washServiceRepository.findById(2L)).thenReturn(Optional.of(service2));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void confirmOrderPayment_WithExistingPayment() {
        Payment existingPayment = new Payment();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.getPaymentByPaymongoIntentId("pay_existing"))
                .thenReturn(Optional.of(existingPayment));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.confirmOrderPayment(1L, "pay_existing", "CARD");

        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void updateOrderStatus_ToCompleted() {
        order.setStatus("CONFIRMED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_ToDiliveryFailed() {
        order.setStatus("CONFIRMED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, "DELIVERY_FAILED");

        assertEquals("DELIVERY_FAILED", result.getStatus());
    }

    @Test
    void getOrderByOrderNumber_Found() {
        when(orderRepository.findByOrderNumber("WM-12345")).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderByOrderNumber("WM-12345");

        assertTrue(result.isPresent());
    }

    @Test
    void getOrderByOrderNumber_NotFound() {
        when(orderRepository.findByOrderNumber("INVALID")).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderByOrderNumber("INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void initiateOrderPayment_WithDifferentPaymentMethod() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.createAndSaveOrderPayment(anyLong(), any(BigDecimal.class), anyString()))
                .thenReturn(new Payment());

        Payment result = orderService.initiateOrderPayment(1L, "CARD");

        assertNotNull(result);
    }

    @Test
    void toOrderDTO_Success() {
        order.setOrderId(1L);
        order.setOrderNumber("WM-12345");
        order.setStatus("PENDING");
        order.setTotalAmount(new BigDecimal("100.00"));
        
        OrderServiceDetail detail = OrderServiceDetail.builder()
                .orderServiceId(1L)
                .service(washService)
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();
        order.setOrderServices(Arrays.asList(detail));

        OrderDTO dto = orderService.toOrderDTO(order);

        assertNotNull(dto);
        assertEquals(1L, dto.getOrderId());
        assertEquals("WM-12345", dto.getOrderNumber());
        assertEquals("PENDING", dto.getStatus());
        assertEquals(new BigDecimal("100.00"), dto.getTotalAmount());
        assertEquals(1, dto.getServices().size());
    }

    @Test
    void toOrderDTO_NullCustomer() {
        order.setCustomer(null);
        order.setOrderServices(new ArrayList<>());

        OrderDTO dto = orderService.toOrderDTO(order);

        assertNotNull(dto);
        assertNull(dto.getCustomerId());
    }

    @Test
    void toOrderDTOs_Success() {
        List<Order> orders = Arrays.asList(order);

        List<OrderDTO> dtos = orderService.toOrderDTOs(orders);

        assertEquals(1, dtos.size());
        assertNotNull(dtos.get(0));
    }

    @Test
    void toPaymentDTO_OrderPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod("GCASH");
        payment.setPaymentStatus("COMPLETED");
        payment.setReferenceType("ORDER");
        payment.setReferenceId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        PaymentDTO dto = orderService.toPaymentDTO(payment);

        assertNotNull(dto);
        assertEquals(1L, dto.getPaymentId());
        assertEquals("COMPLETED", dto.getPaymentStatus());
    }

    @Test
    void getOrderPayments_Success() {
        Payment payment = new Payment();
        when(paymentService.getOrderPayments(1L)).thenReturn(Arrays.asList(payment));

        List<Payment> result = orderService.getOrderPayments(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getCompletedOrderPayment_Found() {
        Payment payment = new Payment();
        when(paymentService.getCompletedOrderPayment(1L)).thenReturn(Optional.of(payment));

        Optional<Payment> result = orderService.getCompletedOrderPayment(1L);

        assertTrue(result.isPresent());
    }

    @Test
    void getCompletedOrderPayment_NotFound() {
        when(paymentService.getCompletedOrderPayment(999L)).thenReturn(Optional.empty());

        Optional<Payment> result = orderService.getCompletedOrderPayment(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllOrders_Success() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));

        List<Order> result = orderService.getAllOrders();

        assertEquals(1, result.size());
    }

    @Test
    void createOrder_WithPickupAddress_Success() {
        orderRequest.setPickupAddressId(1L);
        edu.cit.sevilla.washmate.features.users.Address address = new edu.cit.sevilla.washmate.features.users.Address();
        address.setAddressId(1L);
        
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void createOrder_ThrowsException_WhenInvalidPickupAddress() {
        orderRequest.setPickupAddressId(999L);
        when(addressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(orderRequest, customer);
        });
    }

    @Test
    void createOrder_WithPickupAddressString_Success() {
        orderRequest.setPickupAddressString("123 Street");
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(addressRepository.save(any(edu.cit.sevilla.washmate.features.users.Address.class)))
                .thenReturn(new edu.cit.sevilla.washmate.features.users.Address());

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void createOrder_WithServiceVariant_Success() {
        orderRequest.getServices().get(0).setSelectedVariantId(1L);
        ServiceVariant variant = new ServiceVariant();
        variant.setVariantPrice(new BigDecimal("120.00"));

        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(serviceVariantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void createOrder_ThrowsException_WhenInvalidVariant() {
        orderRequest.getServices().get(0).setSelectedVariantId(999L);
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(serviceVariantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(orderRequest, customer);
        });
    }

    @Test
    void createOrder_WithPremiumUser_FreeDelivery() {
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        edu.cit.sevilla.washmate.features.subscriptions.UserSubscription sub = new edu.cit.sevilla.washmate.features.subscriptions.UserSubscription();
        sub.setStatus("ACTIVE");
        edu.cit.sevilla.washmate.features.subscriptions.Subscription plan = new edu.cit.sevilla.washmate.features.subscriptions.Subscription();
        plan.setPlanType("PREMIUM");
        sub.setSubscription(plan);
        
        when(userSubscriptionRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(sub));

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void updateOrderStatus_CancelPending_Success() {
        order.setStatus("PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, "CANCELLED");

        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    void toPaymentDTO_NonOrderPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod("GCASH");
        payment.setPaymentStatus("COMPLETED");
        payment.setReferenceType("WALLET_TOPUP");
        payment.setReferenceId(1L);

        PaymentDTO dto = orderService.toPaymentDTO(payment);

        assertNotNull(dto);
        assertEquals(1L, dto.getPaymentId());
        assertNull(dto.getOrderId());
    }

    @Test
    void createOrder_WithNonPremiumUser_RegularDelivery() {
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        edu.cit.sevilla.washmate.features.subscriptions.UserSubscription sub = new edu.cit.sevilla.washmate.features.subscriptions.UserSubscription();
        sub.setStatus("ACTIVE");
        edu.cit.sevilla.washmate.features.subscriptions.Subscription plan = new edu.cit.sevilla.washmate.features.subscriptions.Subscription();
        plan.setPlanType("BASIC");
        sub.setSubscription(plan);
        
        when(userSubscriptionRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(sub));

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void createOrder_WithExpiredPremiumUser_RegularDelivery() {
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        edu.cit.sevilla.washmate.features.subscriptions.UserSubscription sub = new edu.cit.sevilla.washmate.features.subscriptions.UserSubscription();
        sub.setStatus("EXPIRED");
        edu.cit.sevilla.washmate.features.subscriptions.Subscription plan = new edu.cit.sevilla.washmate.features.subscriptions.Subscription();
        plan.setPlanType("PREMIUM");
        sub.setSubscription(plan);
        
        when(userSubscriptionRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(sub));

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void createOrder_WithHighAmount_FreeDelivery() {
        orderRequest.getServices().get(0).setQuantity(new BigDecimal("5")); // 100 * 5 = 500 >= 400
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userSubscriptionRepository.findByUserUserId(1L)).thenReturn(Arrays.asList());

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void createOrder_WithTotalWeight_Success() {
        orderRequest.setTotalWeight(new BigDecimal("10.0"));
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }

    @Test
    void createOrder_WithMultipleSubscriptions_Success() {
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        edu.cit.sevilla.washmate.features.subscriptions.UserSubscription sub1 = new edu.cit.sevilla.washmate.features.subscriptions.UserSubscription();
        sub1.setStatus("ACTIVE");
        edu.cit.sevilla.washmate.features.subscriptions.Subscription plan1 = new edu.cit.sevilla.washmate.features.subscriptions.Subscription();
        plan1.setPlanType("BASIC");
        sub1.setSubscription(plan1);

        edu.cit.sevilla.washmate.features.subscriptions.UserSubscription sub2 = new edu.cit.sevilla.washmate.features.subscriptions.UserSubscription();
        sub2.setStatus("ACTIVE");
        edu.cit.sevilla.washmate.features.subscriptions.Subscription plan2 = new edu.cit.sevilla.washmate.features.subscriptions.Subscription();
        plan2.setPlanType("PREMIUM");
        sub2.setSubscription(plan2);
        
        when(userSubscriptionRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(sub1, sub2));

        Order result = orderService.createOrder(orderRequest, customer);

        assertNotNull(result);
    }
}
