package edu.cit.sevilla.washmate.features.wallet;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.orders.Order;
import edu.cit.sevilla.washmate.features.payments.Payment;

@Data
public class WalletTransactionDTO {
    private Long transactionId;
    private Long walletId;
    private BigDecimal amount;
    private String transactionType;  // CREDIT, DEBIT
    private String referenceType;    // ORDER, PAYMENT, SUBSCRIPTION, etc.
    private Long referenceId;
    private String status;           // PENDING, COMPLETED, FAILED
    private String description;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

