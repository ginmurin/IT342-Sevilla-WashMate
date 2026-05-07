package edu.cit.sevilla.washmate.features.payments;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.orders.Order;

@Data
public class PaymentDTO {
    private Long paymentId;
    private Long orderId;  // Legacy field for backward compatibility
    private String orderNumber;  // Legacy field for backward compatibility
    private String referenceType;  // Polymorphic reference type (ORDER, SUBSCRIPTION, WALLET_TOPUP)
    private Long referenceId;      // Polymorphic reference ID
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private String paymongoPaymentIntentId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
}

