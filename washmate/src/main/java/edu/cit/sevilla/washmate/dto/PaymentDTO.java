package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String transactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
}
