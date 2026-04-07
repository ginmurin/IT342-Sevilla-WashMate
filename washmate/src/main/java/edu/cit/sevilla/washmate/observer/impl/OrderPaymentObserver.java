package edu.cit.sevilla.washmate.observer.impl;

import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.observer.PaymentObserver;
import edu.cit.sevilla.washmate.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Observer for payment events related to orders.
 * Handles updating order status when payment is confirmed/failed/cancelled.
 */
@Component
@RequiredArgsConstructor
public class OrderPaymentObserver implements PaymentObserver {

    private final OrderService orderService;

    @Override
    public void onPaymentConfirmed(Payment payment) {
        // When order payment confirmed, update order status
        if ("ORDER".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: OrderPaymentObserver - Payment confirmed for order: " + payment.getReferenceId());
            // Update order status to CONFIRMED
            // orderService.updateOrderStatus(payment.getReferenceId(), "CONFIRMED");
        }
    }

    @Override
    public void onPaymentFailed(Payment payment) {
        // When order payment fails, reset order to unpaid state
        if ("ORDER".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: OrderPaymentObserver - Payment failed for order: " + payment.getReferenceId());
            // Update order status back to PENDING
            // orderService.updateOrderStatus(payment.getReferenceId(), "PENDING");
        }
    }

    @Override
    public void onPaymentCancelled(Payment payment) {
        // When order payment cancelled, reset order status
        if ("ORDER".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: OrderPaymentObserver - Payment cancelled for order: " + payment.getReferenceId());
            // Update order status to CANCELLED
            // orderService.updateOrderStatus(payment.getReferenceId(), "CANCELLED");
        }
    }

    @Override
    public String getPaymentReferenceType() {
        return "ORDER";
    }
}
