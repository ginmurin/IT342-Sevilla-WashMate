package edu.cit.sevilla.washmate.factory;

import edu.cit.sevilla.washmate.entity.Payment;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Factory implementation for creating WALLET_TOPUP payments.
 * Creates payments linked to WalletTransaction entities via polymorphic reference.
 */
@Component
public class WalletTopupPaymentFactory implements PaymentFactory {

    @Override
    public Payment createPayment(Long walletTransactionId, BigDecimal amount, String paymentMethod) {
        return Payment.builder()
                .referenceType("WALLET_TOPUP")
                .referenceId(walletTransactionId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus("PENDING")
                .build();
    }
}
