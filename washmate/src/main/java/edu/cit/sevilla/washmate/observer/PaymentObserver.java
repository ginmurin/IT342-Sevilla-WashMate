package edu.cit.sevilla.washmate.observer;

import edu.cit.sevilla.washmate.entity.Payment;

/**
 * Observer interface for payment events.
 *
 * Implements the Observer pattern to decouple payment confirmation side-effects.
 * When a payment is confirmed, all observers are notified to handle their respective
 * business logic (update order, activate subscription, etc).
 *
 * Benefits:
 * - Decouples payment service from order/subscription/wallet logic
 * - Easy to add new observers without modifying PaymentService
 * - Cleaner code with single responsibility principle
 * - Enables asynchronous processing of payment side-effects
 *
 * Implementation Examples:
 * - OrderPaymentObserver: Update order status when payment confirmed
 * - SubscriptionPaymentObserver: Activate subscription when payment confirmed
 * - WalletPaymentObserver: Deduct from wallet or add balance for topup
 */
public interface PaymentObserver {

    /**
     * Called when a payment is confirmed.
     * Implement side-effect logic (e.g., update order, activate subscription).
     *
     * @param payment The confirmed Payment entity
     */
    void onPaymentConfirmed(Payment payment);

    /**
     * Called when a payment fails.
     * Implement failure handling logic (e.g., reset order status).
     *
     * @param payment The failed Payment entity
     */
    void onPaymentFailed(Payment payment);

    /**
     * Called when a payment is cancelled.
     * Implement cancellation logic (e.g., refund wallet deduction).
     *
     * @param payment The cancelled Payment entity
     */
    void onPaymentCancelled(Payment payment);

    /**
     * Get the type of payment this observer handles.
     * Used to filter observers for specific payment types.
     * @return The reference type (e.g., "ORDER", "SUBSCRIPTION", "WALLET_TOPUP")
     */
    String getPaymentReferenceType();
}
