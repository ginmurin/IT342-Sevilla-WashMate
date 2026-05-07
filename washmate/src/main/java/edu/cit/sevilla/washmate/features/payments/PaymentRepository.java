package edu.cit.sevilla.washmate.features.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.orders.Order;
import edu.cit.sevilla.washmate.features.wallet.Wallet;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymongoPaymentIntentId(String paymongoPaymentIntentId);

    // ===== NEW POLYMORPHIC QUERY METHODS =====

    /**
     * Find payments by polymorphic reference (referenceType + referenceId).
     */
    List<Payment> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    /**
     * Find payment by polymorphic reference and payment status.
     */
    Optional<Payment> findByReferenceTypeAndReferenceIdAndPaymentStatus(
            String referenceType, Long referenceId, String paymentStatus);

    /**
     * Find payments by reference type only.
     */
    List<Payment> findByReferenceType(String referenceType);

    /**
     * Find payments by reference type and payment status.
     */
    List<Payment> findByReferenceTypeAndPaymentStatus(String referenceType, String paymentStatus);

    // ===== CONVENIENCE METHODS WITH DEFAULT IMPLEMENTATIONS =====

    /**
     * Find payments for a specific order using polymorphic pattern.
     */
    default List<Payment> findOrderPayments(Long orderId) {
        return findByReferenceTypeAndReferenceId("ORDER", orderId);
    }

    /**
     * Find payments for a specific subscription using polymorphic pattern.
     */
    default List<Payment> findSubscriptionPayments(Long subscriptionId) {
        return findByReferenceTypeAndReferenceId("SUBSCRIPTION", subscriptionId);
    }

    /**
     * Find payments for a specific wallet top-up using polymorphic pattern.
     */
    default List<Payment> findWalletTopupPayments(Long walletTransactionId) {
        return findByReferenceTypeAndReferenceId("WALLET_TOPUP", walletTransactionId);
    }

    /**
     * Find completed payment for a specific order.
     */
    default Optional<Payment> findCompletedOrderPayment(Long orderId) {
        return findByReferenceTypeAndReferenceIdAndPaymentStatus("ORDER", orderId, "COMPLETED");
    }

    /**
     * Find completed payment for a specific subscription.
     */
    default Optional<Payment> findCompletedSubscriptionPayment(Long subscriptionId) {
        return findByReferenceTypeAndReferenceIdAndPaymentStatus("SUBSCRIPTION", subscriptionId, "COMPLETED");
    }
}

