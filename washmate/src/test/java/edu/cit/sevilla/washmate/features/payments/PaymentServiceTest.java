package edu.cit.sevilla.washmate.features.payments;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .paymentId(1L)
                .referenceType("ORDER")
                .referenceId(1L)
                .amount(new BigDecimal("100.00"))
                .paymentMethod("GCASH")
                .paymentStatus("PENDING")
                .build();
    }

    @Test
    void createOrderPayment_Success() {
        Payment result = paymentService.createOrderPayment(1L, new BigDecimal("100.00"), "GCASH");

        assertEquals("ORDER", result.getReferenceType());
        assertEquals(1L, result.getReferenceId());
        assertEquals("PENDING", result.getPaymentStatus());
    }

    @Test
    void createSubscriptionPayment_Success() {
        Payment result = paymentService.createSubscriptionPayment(1L, new BigDecimal("299.00"), "GCASH");

        assertEquals("SUBSCRIPTION", result.getReferenceType());
        assertEquals(1L, result.getReferenceId());
    }

    @Test
    void createWalletTopupPayment_Success() {
        Payment result = paymentService.createWalletTopupPayment(1L, new BigDecimal("500.00"), "GCASH");

        assertEquals("WALLET_TOPUP", result.getReferenceType());
        assertEquals(1L, result.getReferenceId());
    }

    @Test
    void completePayment_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.completePayment(1L, "pay_123");

        assertEquals("COMPLETED", result.getPaymentStatus());
        assertEquals("pay_123", result.getPaymongoPaymentIntentId());
        assertNotNull(result.getPaymentDate());
    }

    @Test
    void updatePaymentStatus_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.updatePaymentStatus(1L, "FAILED");

        assertEquals("FAILED", result.getPaymentStatus());
    }

    @Test
    void getPaymentsByReference_Success() {
        when(paymentRepository.findByReferenceTypeAndReferenceId("ORDER", 1L))
                .thenReturn(Arrays.asList(payment));

        List<Payment> result = paymentService.getPaymentsByReference("ORDER", 1L);

        assertEquals(1, result.size());
    }

    @Test
    void getCompletedPaymentByReference_Success() {
        payment.setPaymentStatus("COMPLETED");
        when(paymentRepository.findByReferenceTypeAndReferenceIdAndPaymentStatus("ORDER", 1L, "COMPLETED"))
                .thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getCompletedPaymentByReference("ORDER", 1L);

        assertTrue(result.isPresent());
        assertEquals("COMPLETED", result.get().getPaymentStatus());
    }

    @Test
    void getPaymentByPaymongoIntentId_Success() {
        when(paymentRepository.findByPaymongoPaymentIntentId("pay_123")).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getPaymentByPaymongoIntentId("pay_123");

        assertTrue(result.isPresent());
    }

    @Test
    void createAndSaveOrderPayment_Success() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createAndSaveOrderPayment(1L, new BigDecimal("100.00"), "GCASH");

        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
    }
}
