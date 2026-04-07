package edu.cit.sevilla.washmate.strategy.impl;

import edu.cit.sevilla.washmate.strategy.PaymentMethodStrategy;
import edu.cit.sevilla.washmate.strategy.PaymentStrategyContext;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation for WALLET payment method.
 * Handles validation and processing of wallet balance payments.
 * Unlike other payment methods, this directly deducts from user's wallet.
 */
@Component
public class WalletPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public String getPaymentMethod() {
        return "WALLET";
    }

    @Override
    public boolean validate(PaymentStrategyContext context) {
        if (context.getAmount() == null || context.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount for wallet payment");
        }

        // Wallet payment requires amount to be specified
        // Balance check is done at service level before processing
        return true;
    }

    @Override
    public boolean process(PaymentStrategyContext context) {
        // Wallet payment processing:
        // 1. Deduct amount from wallet balance
        // 2. Create wallet transaction record
        // 3. Mark payment as completed
        // This is handled by WalletService at transaction level
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Wallet Balance";
    }

    /**
     * Wallet payments may not always be available (no sufficient balance).
     * Availability check should be done before showing this option.
     */
    @Override
    public boolean isAvailable() {
        // This should check user's wallet balance at runtime
        // For now, assume always available (check done at service level)
        return true;
    }
}
