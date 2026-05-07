package edu.cit.sevilla.washmate.features.payments;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.wallet.WalletTransaction;
import edu.cit.sevilla.washmate.features.orders.Order;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    // Polymorphic reference fields (following WalletTransaction pattern)
    /** Reference type: ORDER | SUBSCRIPTION | WALLET_TOPUP */
    @Column(name = "reference_type")
    private String referenceType;

    /** ID of the referenced entity (order_id, user_subscription_id, or wallet_transaction_id) */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /** e.g. CASH, GCASH, PAYMAYA, CARD */
    @Column(name = "payment_method")
    private String paymentMethod;

    /** PENDING, COMPLETED, FAILED, REFUNDED */
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private String paymentStatus = "PENDING";

    @Column(name = "paymongo_payment_intent_id", unique = true)
    private String paymongoPaymentIntentId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

