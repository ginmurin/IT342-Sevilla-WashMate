package edu.cit.sevilla.washmate.factory;

import edu.cit.sevilla.washmate.entity.Payment;
import java.math.BigDecimal;

/**
 * Factory interface for creating different types of payments.
 *
 * Encapsulates the creation logic for different payment contexts:
 * - ORDER: Payment for a laundry order
 * - SUBSCRIPTION: Payment for subscription upgrade
 * - WALLET_TOPUP: Payment for wallet balance top-up
 *
 * Benefits:
 * - Reduces coupling between PaymentService and payment creation logic
 * - Makes it easy to add new payment types without modifying PaymentService
 * - Centralizes payment validation and business rules
 * - Follows Open/Closed Principle: open for extension, closed for modification
 */
public interface PaymentFactory {

    /**
     * Create a payment with the given parameters.
     *
     * @param referenceId The ID of the entity this payment is for (Order ID, Subscription ID, etc)
     * @param amount The payment amount
     * @param paymentMethod The payment method (CARD, GCASH, MAYA, GRABPAY, WALLET)
     * @return The created Payment entity
     */
    Payment createPayment(Long referenceId, BigDecimal amount, String paymentMethod);
}
