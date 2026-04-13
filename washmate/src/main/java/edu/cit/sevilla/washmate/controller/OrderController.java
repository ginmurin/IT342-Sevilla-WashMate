package edu.cit.sevilla.washmate.controller;

import edu.cit.sevilla.washmate.dto.OrderDTO;
import edu.cit.sevilla.washmate.dto.OrderRequest;
import edu.cit.sevilla.washmate.dto.PaymentDTO;
import edu.cit.sevilla.washmate.entity.Order;
import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for order management with polymorphic payment integration.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    // ===== ORDER CREATION =====

    /**
     * Create a new laundry order.
     */
    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(
            @Validated @RequestBody OrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderService.createOrder(request, user);
        return ResponseEntity.ok(orderService.toOrderDTO(order));
    }

    // ===== ORDER PAYMENT INTEGRATION =====

    /**
     * Initiate payment for an order using polymorphic payment pattern.
     */
    @PostMapping("/{orderId}/payment/initiate")
    public ResponseEntity<PaymentDTO> initiateOrderPayment(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {

        String paymentMethod = request.getOrDefault("paymentMethod", "CARD");

        // Verify order belongs to user
        verifyOrderOwnership(orderId, jwt);

        Payment payment = orderService.initiateOrderPayment(orderId, paymentMethod);
        return ResponseEntity.ok(orderService.toPaymentDTO(payment));
    }

    /**
     * Confirm order payment and update order status.
     */
    @PostMapping("/{orderId}/payment/confirm/{paymentId}")
    public ResponseEntity<OrderDTO> confirmOrderPayment(
            @PathVariable Long orderId,
            @PathVariable String paymentId,
            @RequestParam(required = false, defaultValue = "CARD") String paymentMethod,
            @AuthenticationPrincipal Jwt jwt) {

        // Verify order belongs to user
        verifyOrderOwnership(orderId, jwt);

        Order order = orderService.confirmOrderPayment(orderId, paymentId, paymentMethod);
        return ResponseEntity.ok(orderService.toOrderDTO(order));
    }

    // ===== ORDER MANAGEMENT =====

    /**
     * Update order status.
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {

        String newStatus = request.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        // Verify order belongs to user
        verifyOrderOwnership(orderId, jwt);

        Order order = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(orderService.toOrderDTO(order));
    }

    /**
     * Cancel an order.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {

        // Verify order belongs to user
        verifyOrderOwnership(orderId, jwt);

        Order order = orderService.updateOrderStatus(orderId, "CANCELLED");
        return ResponseEntity.ok(orderService.toOrderDTO(order));
    }

    // ===== ORDER QUERIES =====

    /**
     * Get current user's orders.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderService.getOrdersByCustomer(user.getUserId());
        return ResponseEntity.ok(orderService.toOrderDTOs(orders));
    }

    /**
     * Get order by ID (if user owns it).
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {

        // Verify order belongs to user
        verifyOrderOwnership(orderId, jwt);

        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return ResponseEntity.ok(orderService.toOrderDTO(order));
    }

    /**
     * Get orders by status for current user.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(
            @PathVariable String status,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> allOrders = orderService.getOrdersByStatus(status);

        // Filter to only include current user's orders
        List<Order> userOrders = allOrders.stream()
                .filter(order -> order.getCustomer().getUserId().equals(user.getUserId()))
                .toList();

        return ResponseEntity.ok(orderService.toOrderDTOs(userOrders));
    }

    // ===== ORDER PAYMENT QUERIES =====

    /**
     * Get payments for a specific order.
     */
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<List<PaymentDTO>> getOrderPayments(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {

        // Verify order belongs to user
        verifyOrderOwnership(orderId, jwt);

        List<Payment> payments = orderService.getOrderPayments(orderId);
        List<PaymentDTO> paymentDTOs = payments.stream()
                .map(orderService::toPaymentDTO)
                .toList();

        return ResponseEntity.ok(paymentDTOs);
    }

    /**
     * Get completed payment for an order.
     */
    @GetMapping("/{orderId}/payment")
    public ResponseEntity<PaymentDTO> getOrderPayment(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {

        // Verify order belongs to user
        verifyOrderOwnership(orderId, jwt);

        Payment payment = orderService.getCompletedOrderPayment(orderId)
                .orElse(null);

        if (payment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(orderService.toPaymentDTO(payment));
    }

    // ===== HELPER METHODS =====

    /**
     * Verify that the order belongs to the authenticated user.
     */
    private void verifyOrderOwnership(Long orderId, Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied: Order does not belong to current user");
        }
    }
}