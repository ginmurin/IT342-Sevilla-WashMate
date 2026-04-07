package edu.cit.sevilla.washmate.strategy.impl;

import edu.cit.sevilla.washmate.strategy.PaymentMethodStrategy;
import edu.cit.sevilla.washmate.strategy.PaymentStrategyContext;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation for MAYA payment method.
 * Handles validation and processing of PayMaya wallet payments via PayMongo.
 */
@Component
public class MayaPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public String getPaymentMethod() {
        return "MAYA";
    }

    @Override
    public boolean validate(PaymentStrategyContext context) {
        if (context.getAmount() == null || context.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount for Maya payment");
        }

        // Maya has minimum transaction amount
        if (context.getAmount().compareTo(java.math.BigDecimal.valueOf(1.0)) < 0) {
            throw new IllegalArgumentException("Maya minimum transaction amount is ₱1.00");
        }

        return true;
    }

    @Override
    public boolean process(PaymentStrategyContext context) {
        // Maya processing is handled by PayMongo integration
        // This method confirms the payment intent was successfully processed
        return context.getPaymongoPaymentIntentId() != null;
    }

    @Override
    public String getDisplayName() {
        return "PayMaya";
    }
}
