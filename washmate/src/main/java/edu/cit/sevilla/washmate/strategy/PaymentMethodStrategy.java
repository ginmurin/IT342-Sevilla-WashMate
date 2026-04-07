package edu.cit.sevilla.washmate.strategy;

/**
 * Strategy interface for different payment methods.
 *
 * Encapsulates payment method-specific validation, processing, and business rules.
 * Makes it easy to:
 * - Add new payment methods without modifying existing code
 * - Test each payment method in isolation
 * - Handle method-specific validation (e.g., GCash phone format)
 * - Implement method-specific processing logic
 *
 * Implementation Pattern:
 * - Define validation rules specific to each payment method
 * - Handle payment method initialization and confirmation
 * - Encapsulate method-specific error handling
 *
 * Supported Methods: CARD, GCASH, MAYA, GRABPAY, WALLET
 */
public interface PaymentMethodStrategy {

    /**
     * Get the payment method this strategy handles.
     * @return The payment method (e.g., "CARD", "GCASH")
     */
    String getPaymentMethod();

    /**
     * Validate payment details for this payment method.
     * @param paymentDetails The payment details to validate
     * @return true if valid, false otherwise
     * @throws IllegalArgumentException if validation fails with specific error
     */
    boolean validate(PaymentStrategyContext paymentDetails);

    /**
     * Process the payment for this method.
     * This may involve gateway-specific logic, authentication, etc.
     * @param paymentDetails The payment details to process
     * @return true if processing successful, false otherwise
     */
    boolean process(PaymentStrategyContext paymentDetails);

    /**
     * Get a description of this payment method for UI display.
     */
    String getDisplayName();

    /**
     * Check if this payment method is currently available.
     */
    default boolean isAvailable() {
        return true;
    }
}
