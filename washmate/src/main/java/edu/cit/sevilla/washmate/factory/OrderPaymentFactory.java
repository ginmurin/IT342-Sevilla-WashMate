package edu.cit.sevilla.washmate.factory;

import edu.cit.sevilla.washmate.entity.Payment;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Factory implementation for creating ORDER payments.
 * Creates payments linked to Order entities via polymorphic reference.
 */
@Component
public class OrderPaymentFactory implements PaymentFactory {

    @Override
    public Payment createPayment(Long orderId, BigDecimal amount, String paymentMethod) {
        return Payment.builder()
                .referenceType("ORDER")
                .referenceId(orderId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus("PENDING")
                .build();
    }
}
