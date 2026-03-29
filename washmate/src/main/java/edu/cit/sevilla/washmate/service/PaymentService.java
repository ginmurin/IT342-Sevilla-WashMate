package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // ===== POLYMORPHIC PAYMENT CREATION METHODS =====

    /**
     * Create a payment for an order using polymorphic reference.
     */
    public Payment createOrderPayment(Long orderId, BigDecimal amount, String paymentMethod) {
        return Payment.builder()
                .referenceType("ORDER")
                .referenceId(orderId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus("PENDING")
                .build();
    }

    /**
     * Create a payment for a subscription using polymorphic reference.
     */
    public Payment createSubscriptionPayment(Long subscriptionId, BigDecimal amount, String paymentMethod) {
        return Payment.builder()
                .referenceType("SUBSCRIPTION")
                .referenceId(subscriptionId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus("PENDING")
                .build();
    }

    /**
     * Create a payment for a wallet top-up using polymorphic reference.
     */
    public Payment createWalletTopupPayment(Long walletTransactionId, BigDecimal amount, String paymentMethod) {
        return Payment.builder()
                .referenceType("WALLET_TOPUP")
                .referenceId(walletTransactionId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus("PENDING")
                .build();
    }

    // ===== PAYMENT COMPLETION AND MANAGEMENT =====

    /**
     * Complete a payment with PayMongo integration.
     */
    public Payment completePayment(Long paymentId, String paymongoPaymentIntentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setPaymentStatus("COMPLETED");
        payment.setPaymongoPaymentIntentId(paymongoPaymentIntentId);
        payment.setPaymentDate(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Update payment status.
     */
    public Payment updatePaymentStatus(Long paymentId, String newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setPaymentStatus(newStatus);
        return paymentRepository.save(payment);
    }

    /**
     * Save a payment entity.
     */
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    // ===== POLYMORPHIC QUERY METHODS =====

    /**
     * Get payments by polymorphic reference.
     */
    public List<Payment> getPaymentsByReference(String referenceType, Long referenceId) {
        return paymentRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    /**
     * Get completed payment by polymorphic reference.
     */
    public Optional<Payment> getCompletedPaymentByReference(String referenceType, Long referenceId) {
        return paymentRepository.findByReferenceTypeAndReferenceIdAndPaymentStatus(
                referenceType, referenceId, "COMPLETED");
    }

    /**
     * Get payment by PayMongo payment intent ID.
     */
    public Optional<Payment> getPaymentByPaymongoIntentId(String paymongoPaymentIntentId) {
        return paymentRepository.findByPaymongoPaymentIntentId(paymongoPaymentIntentId);
    }

    // ===== CONVENIENCE METHODS FOR SPECIFIC ENTITY TYPES =====

    /**
     * Get payments for a specific order.
     */
    public List<Payment> getOrderPayments(Long orderId) {
        return paymentRepository.findOrderPayments(orderId);
    }

    /**
     * Get payments for a specific subscription.
     */
    public List<Payment> getSubscriptionPayments(Long subscriptionId) {
        return paymentRepository.findSubscriptionPayments(subscriptionId);
    }

    /**
     * Get payments for a specific wallet top-up.
     */
    public List<Payment> getWalletTopupPayments(Long walletTransactionId) {
        return paymentRepository.findWalletTopupPayments(walletTransactionId);
    }

    /**
     * Get completed payment for an order.
     */
    public Optional<Payment> getCompletedOrderPayment(Long orderId) {
        return paymentRepository.findCompletedOrderPayment(orderId);
    }

    /**
     * Get completed payment for a subscription.
     */
    public Optional<Payment> getCompletedSubscriptionPayment(Long subscriptionId) {
        return paymentRepository.findCompletedSubscriptionPayment(subscriptionId);
    }

    // ===== PAYMENT CREATION WITH IMMEDIATE SAVE =====

    /**
     * Create and save order payment in one step.
     */
    public Payment createAndSaveOrderPayment(Long orderId, BigDecimal amount, String paymentMethod) {
        Payment payment = createOrderPayment(orderId, amount, paymentMethod);
        return paymentRepository.save(payment);
    }

    /**
     * Create and save subscription payment in one step.
     */
    public Payment createAndSaveSubscriptionPayment(Long subscriptionId, BigDecimal amount, String paymentMethod) {
        Payment payment = createSubscriptionPayment(subscriptionId, amount, paymentMethod);
        return paymentRepository.save(payment);
    }

    /**
     * Create and save wallet top-up payment in one step.
     */
    public Payment createAndSaveWalletTopupPayment(Long walletTransactionId, BigDecimal amount, String paymentMethod) {
        Payment payment = createWalletTopupPayment(walletTransactionId, amount, paymentMethod);
        return paymentRepository.save(payment);
    }

    // ===== MIGRATION SUPPORT =====

    /**
     * Migrate existing payments to use polymorphic pattern.
     * Call this during application startup or as needed.
     */
    public int migrateExistingPayments() {
        List<Payment> unmigrated = paymentRepository.findByReferenceTypeIsNullAndOrderIsNotNull();

        for (Payment payment : unmigrated) {
            payment.setReferenceType("ORDER");
            payment.setReferenceId(payment.getOrder().getOrderId());
            paymentRepository.save(payment);
        }

        return unmigrated.size();
    }
}