package edu.cit.sevilla.washmate.strategy.impl;

import edu.cit.sevilla.washmate.strategy.PaymentMethodStrategy;
import edu.cit.sevilla.washmate.strategy.PaymentStrategyContext;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation for CARD payment method.
 * Handles validation and processing of credit/debit card payments via PayMongo.
 */
@Component
public class CardPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public String getPaymentMethod() {
        return "CARD";
    }

    @Override
    public boolean validate(PaymentStrategyContext context) {
        if (context.getAmount() == null || context.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount for card payment");
        }
        // Card validation via PayMongo token is handled at gateway level
        return true;
    }

    @Override
    public boolean process(PaymentStrategyContext context) {
        // Card processing is handled by PayMongo integration
        // This method confirms the payment intent was successfully processed
        return context.getPaymongoPaymentIntentId() != null;
    }

    @Override
    public String getDisplayName() {
        return "Credit/Debit Card";
    }
}
