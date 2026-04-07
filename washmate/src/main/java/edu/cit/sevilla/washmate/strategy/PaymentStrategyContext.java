package edu.cit.sevilla.washmate.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Context object for payment strategy execution.
 * Encapsulates all payment details needed by different payment method strategies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStrategyContext {

    private String paymentMethod;
    private BigDecimal amount;
    private Long orderId;
    private Long paymentId;
    private String paymongoPaymentIntentId;

    // Optional fields for specific payment methods
    private String email;
    private String phone;
    private String cardToken;
    private String referenceNumber;
}
