package edu.cit.sevilla.washmate.observer.impl;

import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.observer.PaymentObserver;
import edu.cit.sevilla.washmate.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Observer for payment events related to wallet operations.
 * Handles processing wallet topups when payment is confirmed.
 * Also handles wallet deductions for wallet payment method at order/subscription level.
 */
@Component
@RequiredArgsConstructor
public class WalletPaymentObserver implements PaymentObserver {

    private final WalletService walletService;

    @Override
    public void onPaymentConfirmed(Payment payment) {
        // When wallet topup payment confirmed, add balance to wallet
        if ("WALLET_TOPUP".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: WalletPaymentObserver - Wallet topup confirmed: " + payment.getReferenceId());
            // Process wallet transaction and add balance
            // walletService.processWalletTopup(payment.getReferenceId(), payment.getAmount());
        }
    }

    @Override
    public void onPaymentFailed(Payment payment) {
        // When wallet topup payment fails, mark transaction as failed
        if ("WALLET_TOPUP".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: WalletPaymentObserver - Wallet topup failed: " + payment.getReferenceId());
            // Update transaction status to FAILED
            // walletService.updateTransactionStatus(payment.getReferenceId(), "FAILED");
        }
    }

    @Override
    public void onPaymentCancelled(Payment payment) {
        // When wallet topup payment cancelled, mark transaction as cancelled
        if ("WALLET_TOPUP".equals(payment.getReferenceType())) {
            System.out.println("DEBUG: WalletPaymentObserver - Wallet topup cancelled: " + payment.getReferenceId());
            // Update transaction status to CANCELLED
            // walletService.updateTransactionStatus(payment.getReferenceId(), "CANCELLED");
        }
    }

    @Override
    public String getPaymentReferenceType() {
        return "WALLET_TOPUP";
    }
}
