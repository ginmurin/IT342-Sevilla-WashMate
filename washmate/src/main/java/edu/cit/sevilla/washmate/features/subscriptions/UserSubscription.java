package edu.cit.sevilla.washmate.features.subscriptions;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.payments.Payment;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_subscription_id")
    private Long userSubscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE";  // ACTIVE, CANCELLED, EXPIRED

    // Removed direct Payment FK - now uses polymorphic Payment references
    // Payment relationship handled via Payment.referenceType="SUBSCRIPTION" and Payment.referenceId=userSubscriptionId

    /** PayMongo payment reference for external payment tracking */
    @Column(name = "paymongo_payment_id")
    private String paymongoPaymentId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

