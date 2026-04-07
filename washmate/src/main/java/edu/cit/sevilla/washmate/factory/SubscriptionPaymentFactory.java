package edu.cit.sevilla.washmate.factory;

import edu.cit.sevilla.washmate.entity.Payment;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Factory implementation for creating SUBSCRIPTION payments.
 * Creates payments linked to UserSubscription entities via polymorphic reference.
 */
@Component
public class SubscriptionPaymentFactory implements PaymentFactory {

    @Override
    public Payment createPayment(Long subscriptionId, BigDecimal amount, String paymentMethod) {
        return Payment.builder()
                .referenceType("SUBSCRIPTION")
                .referenceId(subscriptionId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus("PENDING")
                .build();
    }
}
