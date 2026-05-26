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

    // ===== Additional Comprehensive Tests for Branch Coverage =====

    @Test
    void completePayment_NotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.completePayment(999L, "pay_123"));
    }

    @Test
    void updatePaymentStatus_NotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.updatePaymentStatus(999L, "FAILED"));
    }

    @Test
    void savePayment_Success() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.savePayment(payment);

        assertNotNull(result);
        verify(paymentRepository).save(payment);
    }

    @Test
    void getPaymentById_Found() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getPaymentById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getPaymentId());
    }

    @Test
    void getPaymentById_NotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.getPaymentById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getOrderPayments_Success() {
        when(paymentRepository.findOrderPayments(1L))
                .thenReturn(Arrays.asList(payment));

        List<Payment> result = paymentService.getOrderPayments(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getOrderPayments_Empty() {
        when(paymentRepository.findOrderPayments(999L))
                .thenReturn(Arrays.asList());

        List<Payment> result = paymentService.getOrderPayments(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getSubscriptionPayments_Success() {
        payment.setReferenceType("SUBSCRIPTION");
        when(paymentRepository.findSubscriptionPayments(1L))
                .thenReturn(Arrays.asList(payment));

        List<Payment> result = paymentService.getSubscriptionPayments(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getWalletTopupPayments_Success() {
        payment.setReferenceType("WALLET_TOPUP");
        when(paymentRepository.findWalletTopupPayments(1L))
                .thenReturn(Arrays.asList(payment));

        List<Payment> result = paymentService.getWalletTopupPayments(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getCompletedPaymentByReference_NotFound() {
        when(paymentRepository.findByReferenceTypeAndReferenceIdAndPaymentStatus("ORDER", 999L, "COMPLETED"))
                .thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.getCompletedPaymentByReference("ORDER", 999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getPaymentByPaymongoIntentId_NotFound() {
        when(paymentRepository.findByPaymongoPaymentIntentId("invalid_id")).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.getPaymentByPaymongoIntentId("invalid_id");

        assertFalse(result.isPresent());
    }

    @Test
    void createOrderPayment_DifferentPaymentMethod() {
        Payment result = paymentService.createOrderPayment(2L, new BigDecimal("250.50"), "CARD");

        assertEquals("ORDER", result.getReferenceType());
        assertEquals(2L, result.getReferenceId());
        assertEquals("CARD", result.getPaymentMethod());
        assertEquals(new BigDecimal("250.50"), result.getAmount());
    }

    @Test
    void createSubscriptionPayment_WithSave() {
        Payment subPayment = Payment.builder()
                .referenceType("SUBSCRIPTION")
                .referenceId(2L)
                .amount(new BigDecimal("599.00"))
                .paymentMethod("CARD")
                .paymentStatus("PENDING")
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(subPayment);

        Payment created = paymentService.createSubscriptionPayment(2L, new BigDecimal("599.00"), "CARD");
        Payment saved = paymentService.savePayment(created);

        assertNotNull(saved);
        assertEquals("SUBSCRIPTION", saved.getReferenceType());
    }

    @Test
    void completePayment_UpdatesAllFields() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.completePayment(1L, "pay_new_intent");

        assertEquals("COMPLETED", result.getPaymentStatus());
        assertEquals("pay_new_intent", result.getPaymongoPaymentIntentId());
        assertNotNull(result.getPaymentDate());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_MultipleStatusChanges() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        paymentService.updatePaymentStatus(1L, "PROCESSING");
        paymentService.updatePaymentStatus(1L, "COMPLETED");

        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void createAndSaveOrderPayment_WithDifferentAmounts() {
        Payment largePayment = Payment.builder()
                .referenceType("ORDER")
                .referenceId(3L)
                .amount(new BigDecimal("9999.99"))
                .paymentMethod("GCASH")
                .paymentStatus("PENDING")
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(largePayment);

        Payment result = paymentService.createAndSaveOrderPayment(3L, new BigDecimal("9999.99"), "GCASH");

        assertNotNull(result);
        assertEquals(new BigDecimal("9999.99"), result.getAmount());
    }

    @Test
    void createAndSaveSubscriptionPayment_Success() {
        Payment subPayment = Payment.builder()
                .paymentId(2L)
                .referenceType("SUBSCRIPTION")
                .referenceId(2L)
                .amount(new BigDecimal("299.00"))
                .paymentMethod("CARD")
                .paymentStatus("PENDING")
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(subPayment);

        Payment result = paymentService.createAndSaveSubscriptionPayment(2L, new BigDecimal("299.00"), "CARD");

        assertNotNull(result);
        assertEquals("SUBSCRIPTION", result.getReferenceType());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createAndSaveWalletTopupPayment_Success() {
        Payment topupPayment = Payment.builder()
                .paymentId(3L)
                .referenceType("WALLET_TOPUP")
                .referenceId(1L)
                .amount(new BigDecimal("500.00"))
                .paymentMethod("GCASH")
                .paymentStatus("PENDING")
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(topupPayment);

        Payment result = paymentService.createAndSaveWalletTopupPayment(1L, new BigDecimal("500.00"), "GCASH");

        assertNotNull(result);
        assertEquals("WALLET_TOPUP", result.getReferenceType());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getCompletedOrderPayment_Found() {
        payment.setPaymentStatus("COMPLETED");
        when(paymentRepository.findCompletedOrderPayment(1L)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getCompletedOrderPayment(1L);

        assertTrue(result.isPresent());
        assertEquals("COMPLETED", result.get().getPaymentStatus());
    }

    @Test
    void getCompletedOrderPayment_NotFound() {
        when(paymentRepository.findCompletedOrderPayment(999L)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.getCompletedOrderPayment(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getCompletedSubscriptionPayment_Found() {
        payment.setPaymentStatus("COMPLETED");
        payment.setReferenceType("SUBSCRIPTION");
        when(paymentRepository.findCompletedSubscriptionPayment(1L)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getCompletedSubscriptionPayment(1L);

        assertTrue(result.isPresent());
        assertEquals("COMPLETED", result.get().getPaymentStatus());
    }

    @Test
    void getCompletedSubscriptionPayment_NotFound() {
        when(paymentRepository.findCompletedSubscriptionPayment(999L)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.getCompletedSubscriptionPayment(999L);

        assertFalse(result.isPresent());
    }
}
