package edu.cit.sevilla.washmate.facade;

import edu.cit.sevilla.washmate.dto.OrderDTO;
import edu.cit.sevilla.washmate.dto.OrderRequest;
import edu.cit.sevilla.washmate.dto.PaymentDTO;
import edu.cit.sevilla.washmate.entity.Order;
import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.factory.OrderPaymentFactory;
import edu.cit.sevilla.washmate.service.OrderService;
import edu.cit.sevilla.washmate.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade for order creation and payment workflows.
 *
 * Orchestrates complex interactions between:
 * - OrderService: Order creation and management
 * - PaymentService: Payment creation and processing
 * - OrderPaymentFactory: Payment creation for orders
 * - WalletService: Wallet balance management
 *
 * Benefits:
 * - Single point of entry for order creation workflow
 * - Decouples controllers from multiple service dependencies
 * - Centralizes order-payment-wallet coordination logic
 * - Simplifies client code with unified interface
 *
 * Usage:
 *   OrderDTO order = orderFacade.createOrderWithPayment(orderRequest, user);
 *   PaymentDTO payment = orderFacade.initiatePayment(orderId, paymentMethod);
 *   boolean confirmed = orderFacade.confirmPayment(orderId, paymentId, intentId);
 */
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final DTOConverter dtoConverter;
    private final OrderPaymentFactory orderPaymentFactory;
    private final AuthenticationHelper authHelper;

    /**
     * Create an order for an authenticated user.
     * Handles order creation and returns order details.
     *
     * @param orderRequest The order creation request
     * @param user The authenticated user creating the order
     * @return OrderDTO with created order details
     */
    @Transactional
    public OrderDTO createOrder(OrderRequest orderRequest, User user) {
        Order createdOrder = orderService.createOrder(orderRequest, user);
        return dtoConverter.toOrderDTO(createdOrder);
    }

    /**
     * Initiate payment for an order.
     * Creates a payment record and returns payment details.
     *
     * @param orderId The order ID to pay for
     * @param paymentMethod The payment method (CARD, GCASH, MAYA, WALLET, etc)
     * @return PaymentDTO with payment details including paymentId
     */
    @Transactional
    public PaymentDTO initiatePayment(Long orderId, String paymentMethod) {
        // Fetch the order to get amount
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Create payment using factory
        Payment payment = orderPaymentFactory.createPayment(orderId, order.getTotalAmount(), paymentMethod);

        // Save the payment
        Payment savedPayment = paymentService.savePayment(payment);

        // Return payment DTO
        return dtoConverter.toPaymentDTO(savedPayment);
    }

    /**
     * Confirm payment for an order after PayMongo processing.
     * Updates payment status and triggers observers (OrderPaymentObserver).
     *
     * @param orderId The order ID
     * @param paymentId The payment ID to confirm
     * @param paymongoPaymentIntentId The PayMongo payment intent ID for 3DS verification
     * @return true if confirmation successful
     */
    @Transactional
    public boolean confirmPayment(Long orderId, Long paymentId, String paymongoPaymentIntentId) {
        // Complete the payment with PayMongo intent ID
        Payment completedPayment = paymentService.completePayment(paymentId, paymongoPaymentIntentId);

        // Observer pattern will handle order status update
        // (OrderPaymentObserver.onPaymentConfirmed will be called)

        return "COMPLETED".equals(completedPayment.getPaymentStatus());
    }

    /**
     * Get order details by order ID.
     *
     * @param orderId The order ID
     * @return OrderDTO with order details
     */
    public OrderDTO getOrder(Long orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return dtoConverter.toOrderDTO(order);
    }

    /**
     * Get payment details for an order.
     *
     * @param orderId The order ID
     * @return PaymentDTO with payment details
     */
    public PaymentDTO getOrderPayment(Long orderId) {
        Payment payment = paymentService.getPaymentsByReference("ORDER", orderId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

        return dtoConverter.toPaymentDTO(payment);
    }
}
