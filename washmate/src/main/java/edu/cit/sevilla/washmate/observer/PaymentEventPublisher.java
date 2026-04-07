package edu.cit.sevilla.washmate.observer;

import edu.cit.sevilla.washmate.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Publisher for payment events.
 * Manages PaymentObserver instances and notifies them when payments change state.
 * This decouples the PaymentService from order/subscription/wallet services.
 */
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final List<PaymentObserver> observers;

    /**
     * Notify all observers that a payment has been confirmed.
     * @param payment The confirmed payment
     */
    public void publishPaymentConfirmed(Payment payment) {
        observers.stream()
                .filter(observer -> observer.getPaymentReferenceType().equals(payment.getReferenceType()))
                .forEach(observer -> {
                    try {
                        observer.onPaymentConfirmed(payment);
                    } catch (Exception e) {
                        System.err.println("Error notifying observer on payment confirmed: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Notify all observers that a payment has failed.
     * @param payment The failed payment
     */
    public void publishPaymentFailed(Payment payment) {
        observers.stream()
                .filter(observer -> observer.getPaymentReferenceType().equals(payment.getReferenceType()))
                .forEach(observer -> {
                    try {
                        observer.onPaymentFailed(payment);
                    } catch (Exception e) {
                        System.err.println("Error notifying observer on payment failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Notify all observers that a payment has been cancelled.
     * @param payment The cancelled payment
     */
    public void publishPaymentCancelled(Payment payment) {
        observers.stream()
                .filter(observer -> observer.getPaymentReferenceType().equals(payment.getReferenceType()))
                .forEach(observer -> {
                    try {
                        observer.onPaymentCancelled(payment);
                    } catch (Exception e) {
                        System.err.println("Error notifying observer on payment cancelled: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }
}
