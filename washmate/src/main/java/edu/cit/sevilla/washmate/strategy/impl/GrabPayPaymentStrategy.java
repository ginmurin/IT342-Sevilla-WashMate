package edu.cit.sevilla.washmate.strategy.impl;

import edu.cit.sevilla.washmate.strategy.PaymentMethodStrategy;
import edu.cit.sevilla.washmate.strategy.PaymentStrategyContext;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation for GRABPAY payment method.
 * Handles validation and processing of GrabPay payments via PayMongo.
 */
@Component
public class GrabPayPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public String getPaymentMethod() {
        return "GRABPAY";
    }

    @Override
    public boolean validate(PaymentStrategyContext context) {
        if (context.getAmount() == null || context.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount for GrabPay payment");
        }

        // GrabPay has minimum transaction amount
        if (context.getAmount().compareTo(java.math.BigDecimal.valueOf(1.0)) < 0) {
            throw new IllegalArgumentException("GrabPay minimum transaction amount is ₱1.00");
        }

        return true;
    }

    @Override
    public boolean process(PaymentStrategyContext context) {
        // GrabPay processing is handled by PayMongo integration
        // This method confirms the payment intent was successfully processed
        return context.getPaymongoPaymentIntentId() != null;
    }

    @Override
    public String getDisplayName() {
        return "GrabPay";
    }
}
