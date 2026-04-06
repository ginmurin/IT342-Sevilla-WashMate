package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.OrderDTO;
import edu.cit.sevilla.washmate.dto.OrderRequest;
import edu.cit.sevilla.washmate.dto.OrderServiceInput;
import edu.cit.sevilla.washmate.dto.PaymentDTO;
import edu.cit.sevilla.washmate.entity.Order;
import edu.cit.sevilla.washmate.entity.OrderServiceDetail;
import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.WashService;
import edu.cit.sevilla.washmate.entity.ServiceVariant;
import edu.cit.sevilla.washmate.entity.Address;
import edu.cit.sevilla.washmate.repository.OrderRepository;
import edu.cit.sevilla.washmate.repository.AddressRepository;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.repository.WashServiceRepository;
import edu.cit.sevilla.washmate.repository.ServiceVariantRepository;
import edu.cit.sevilla.washmate.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final WashServiceRepository washServiceRepository;
    private final ServiceVariantRepository serviceVariantRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentService paymentService;
    private final EntityManager entityManager;

    // ===== ORDER CREATION AND MANAGEMENT =====

    /**
     * Create a new order for the customer with multiple services.
     */
    @Transactional
    public Order createOrder(OrderRequest request, User customer) {
        log.info("Creating order for customer: {} (ID: {})", customer.getUsername(), customer.getUserId());

        // Validate at least one service is provided
        if (request.getServices() == null || request.getServices().isEmpty()) {
            throw new IllegalArgumentException("At least one service is required");
        }

        // DEBUG: Log received services to identify duplicates
        log.info("Received {} services in request:", request.getServices().size());
        request.getServices().forEach(svc ->
            log.info("  - Service ID: {}, Quantity: {}, VariantID: {}",
                svc.getServiceId(), svc.getQuantity(), svc.getSelectedVariantId())
        );

        // Validate addresses if IDs are provided
        if (request.getPickupAddressId() != null) {
            addressRepository.findById(request.getPickupAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid pickup address ID: " + request.getPickupAddressId()));
        }
        if (request.getDeliveryAddressId() != null) {
            addressRepository.findById(request.getDeliveryAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid delivery address ID: " + request.getDeliveryAddressId()));
        }

        // Generate unique order number
        String orderNumber = "WM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create order without direct service (will be added via orderServices)
        Order order = Order.builder()
                .customer(customer)
                .orderNumber(orderNumber)
                .status("PENDING")
                .totalWeight(request.getTotalWeight())
                .isRushOrder(request.getIsRushOrder())
                .specialInstructions(request.getSpecialInstructions())
                .pickupSchedule(request.getPickupSchedule())
                .deliverySchedule(request.getDeliverySchedule())
                .build();

        // Set addresses: use provided IDs, or create new addresses from address strings
        // Pickup Address
        if (request.getPickupAddressId() != null) {
            addressRepository.findById(request.getPickupAddressId())
                    .ifPresent(order::setPickupAddress);
        } else if (request.getPickupAddressString() != null && !request.getPickupAddressString().isEmpty()) {
            Address pickupAddr = Address.builder()
                    .user(customer)
                    .fullAddress(request.getPickupAddressString())
                    .latitude(request.getPickupLatitude())
                    .longitude(request.getPickupLongitude())
                    .label("Order Pickup")
                    .isDefault(false)
                    .build();
            Address savedPickupAddr = addressRepository.save(pickupAddr);
            order.setPickupAddress(savedPickupAddr);
        }

        // Delivery Address
        if (request.getDeliveryAddressId() != null) {
            addressRepository.findById(request.getDeliveryAddressId())
                    .ifPresent(order::setDeliveryAddress);
        } else if (request.getDeliveryAddressString() != null && !request.getDeliveryAddressString().isEmpty()) {
            Address deliveryAddr = Address.builder()
                    .user(customer)
                    .fullAddress(request.getDeliveryAddressString())
                    .latitude(request.getDeliveryLatitude())
                    .longitude(request.getDeliveryLongitude())
                    .label("Order Delivery")
                    .isDefault(false)
                    .build();
            Address savedDeliveryAddr = addressRepository.save(deliveryAddr);
            order.setDeliveryAddress(savedDeliveryAddr);
        }

        // Add each service to the order
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderServiceInput serviceInput : request.getServices()) {
            WashService service = washServiceRepository.findById(serviceInput.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Service not found with ID: " + serviceInput.getServiceId()));

            // Determine unit price: use variant price if selectedVariantId is provided, otherwise use base price
            BigDecimal unitPrice = service.getBasePricePerUnit();
            if (serviceInput.getSelectedVariantId() != null) {
                // Look up the variant price
                var variant = serviceVariantRepository.findById(serviceInput.getSelectedVariantId())
                        .orElseThrow(() -> new IllegalArgumentException("Variant not found with ID: " + serviceInput.getSelectedVariantId()));
                unitPrice = variant.getVariantPrice();
            }

            // Calculate subtotal for this service
            BigDecimal subtotal = unitPrice.multiply(serviceInput.getQuantity());

            // Create OrderServiceDetail
            OrderServiceDetail detail = OrderServiceDetail.builder()
                    .order(order)
                    .service(service)
                    .quantity(serviceInput.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.getOrderServices().add(detail);
            totalAmount = totalAmount.add(subtotal);
        }

        // Calculate and validate delivery fee server-side
        // Rule: Premium users → FREE, Non-premium + subtotal >= 400 → FREE, otherwise → ₱50
        BigDecimal calculatedDeliveryFee = calculateDeliveryFee(customer, totalAmount);
        order.setDeliveryFee(calculatedDeliveryFee);

        // Set total amount (sum of all service subtotals + delivery fee)
        order.setTotalAmount(totalAmount.add(calculatedDeliveryFee));

        Order savedOrder = orderRepository.save(order);
        // Flush to ensure all OrderServiceDetail rows are written to database
        entityManager.flush();
        // Refresh order entity to load persisted OrderServiceDetail collection from database
        entityManager.refresh(savedOrder);

        log.info("Order created successfully. Order Number: {}, ID: {}, Services: {}",
                savedOrder.getOrderNumber(), savedOrder.getOrderId(), savedOrder.getOrderServices().size());

        return savedOrder;
    }

    /**
     * Calculate total amount for order using actual service pricing.
     */
    private BigDecimal calculateOrderTotal(OrderRequest request, WashService washService) {
        // Use actual service price per unit
        BigDecimal basePrice = washService.getBasePricePerUnit();

        // Calculate based on weight if provided, otherwise use base price
        BigDecimal totalPrice = request.getTotalWeight() != null
                ? washService.getBasePricePerUnit().multiply(request.getTotalWeight())
                : washService.getBasePricePerUnit();

        return totalPrice;
    }

    /**
     * Calculate delivery fee based on subscription and order total.
     * Rule: Premium users → FREE, Non-premium + subtotal >= ₱400 → FREE, otherwise → ₱50
     */
    private BigDecimal calculateDeliveryFee(User customer, BigDecimal orderSubtotal) {
        // Check if customer has an active PREMIUM subscription
        boolean isPremium = userSubscriptionRepository != null &&
                userSubscriptionRepository.findByUserUserId(customer.getUserId()).stream()
                .anyMatch(sub -> "PREMIUM".equals(sub.getSubscription().getPlanType()) &&
                         "ACTIVE".equals(sub.getStatus()));

        // Premium users get free delivery
        if (isPremium) {
            return BigDecimal.ZERO;
        }

        // Non-premium: free delivery if order >= ₱400, else ₱50
        if (orderSubtotal.compareTo(new BigDecimal("400")) >= 0) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal("50");
    }

    // ===== PAYMENT INTEGRATION =====

    /**
     * Initiate payment for an order using polymorphic pattern.
     */
    @Transactional
    public Payment initiateOrderPayment(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Order is not in pending status");
        }

        // Create polymorphic payment
        return paymentService.createAndSaveOrderPayment(
                orderId,
                order.getTotalAmount(),
                paymentMethod
        );
    }

    /**
     * Confirm order payment and update order status.
     */
    @Transactional
    public Order confirmOrderPayment(Long orderId, String paymentId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Check if polymorphic payment exists for this order
        Optional<Payment> existingPayment = paymentService.getPaymentByPaymongoIntentId(paymentId);

        Payment payment;
        if (existingPayment.isPresent()) {
            payment = existingPayment.get();
        } else {
            // Create new polymorphic payment for this order
            payment = Payment.builder()
                    .referenceType("ORDER")
                    .referenceId(orderId)
                    .amount(order.getTotalAmount())
                    .paymentMethod(paymentMethod)
                    .paymentStatus("COMPLETED")
                    .paymongoPaymentIntentId(paymentId)
                    .paymentDate(LocalDateTime.now())
                    .build();

            payment = paymentService.savePayment(payment);
        }

        // Update order status
        order.setStatus("CONFIRMED");
        return orderRepository.save(order);
    }

    // ===== ORDER STATUS MANAGEMENT =====

    /**
     * Update order status.
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Validate status transitions (you can add more business rules here)
        if ("CANCELLED".equals(newStatus) && !"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    // ===== ORDER RETRIEVAL =====

    /**
     * Get orders by customer ID.
     */
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerUserId(customerId);
    }

    /**
     * Get order by ID.
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Get order by order number.
     */
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * Get orders by status.
     */
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    // ===== DTO CONVERSION METHODS =====

    /**
     * Convert Order entity to DTO with all services.
     */
    public OrderDTO toOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTotalWeight(order.getTotalWeight());
        dto.setIsRushOrder(order.getIsRushOrder());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setPickupSchedule(order.getPickupSchedule());
        dto.setDeliverySchedule(order.getDeliverySchedule());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Set customer information
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getUserId());
            dto.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        }

        // Set address information
        if (order.getPickupAddress() != null) {
            dto.setPickupAddressId(order.getPickupAddress().getAddressId());
        }
        if (order.getDeliveryAddress() != null) {
            dto.setDeliveryAddressId(order.getDeliveryAddress().getAddressId());
        }

        // Map all services from order_services junction table
        if (order.getOrderServices() != null && !order.getOrderServices().isEmpty()) {
            dto.setServices(order.getOrderServices().stream()
                    .map(detail -> new OrderDTO.OrderServiceDTO(
                            detail.getOrderServiceId(),
                            detail.getService().getServiceId(),
                            detail.getService().getServiceName(),
                            detail.getQuantity(),
                            detail.getUnitPrice(),
                            detail.getSubtotal()
                    ))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Convert list of Order entities to DTOs.
     */
    public List<OrderDTO> toOrderDTOs(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Payment entity to DTO for order payments.
     */
    public PaymentDTO toPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymongoPaymentIntentId(payment.getPaymongoPaymentIntentId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());

        // For polymorphic payments, populate orderId if it's an order payment
        if ("ORDER".equals(payment.getReferenceType())) {
            dto.setOrderId(payment.getReferenceId());

            // Get order number if possible
            orderRepository.findById(payment.getReferenceId())
                    .ifPresent(order -> dto.setOrderNumber(order.getOrderNumber()));
        }

        return dto;
    }

    // ===== ORDER PAYMENT QUERIES =====

    /**
     * Get payments for a specific order.
     */
    public List<Payment> getOrderPayments(Long orderId) {
        return paymentService.getOrderPayments(orderId);
    }

    /**
     * Get completed payment for an order.
     */
    public Optional<Payment> getCompletedOrderPayment(Long orderId) {
        return paymentService.getCompletedOrderPayment(orderId);
    }
}