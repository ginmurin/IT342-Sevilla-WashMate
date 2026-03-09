package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long paymentId;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private String paymongoPaymentIntentId;
    private String transactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
}
