package edu.cit.sevilla.washmate.observer.impl;

import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.observer.PaymentObserver;
import edu.cit.sevilla.washmate.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Observer for payment events related to subscriptions.
 * Handles activating/deactivating subscriptions when payment is confirmed/failed/cancelled.
 */
@Component
@RequiredArgsConstructor
public class SubscriptionPaymentObserver implements PaymentObserver {

    private final SubscriptionService subscriptionService;

    @Override
    public void onPaymentConfirmed(Payment payment) {
        // When subscription payment confirmed, activate the subscription
        if ("SUBSCRIPTION".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: SubscriptionPaymentObserver - Payment confirmed for subscription: " + payment.getReferenceId());
            // Activate subscription and set start date
            // subscriptionService.activateSubscription(payment.getReferenceId());
        }
    }

    @Override
    public void onPaymentFailed(Payment payment) {
        // When subscription payment fails, keep subscription pending
        if ("SUBSCRIPTION".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: SubscriptionPaymentObserver - Payment failed for subscription: " + payment.getReferenceId());
            // Update subscription status to PENDING
            // subscriptionService.updateSubscriptionStatus(payment.getReferenceId(), "PENDING");
        }
    }

    @Override
    public void onPaymentCancelled(Payment payment) {
        // When subscription payment cancelled, reset or cancel subscription
        if ("SUBSCRIPTION".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: SubscriptionPaymentObserver - Payment cancelled for subscription: " + payment.getReferenceId());
            // Update subscription status to CANCELLED
            // subscriptionService.updateSubscriptionStatus(payment.getReferenceId(), "CANCELLED");
        }
    }

    @Override
    public String getPaymentReferenceType() {
        return "SUBSCRIPTION";
    }
}
