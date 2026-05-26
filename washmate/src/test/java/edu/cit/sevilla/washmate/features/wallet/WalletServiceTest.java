package edu.cit.sevilla.washmate.features.wallet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import edu.cit.sevilla.washmate.features.notifications.NotificationService;
import edu.cit.sevilla.washmate.features.payments.Payment;
import edu.cit.sevilla.washmate.features.payments.PaymentService;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        wallet = Wallet.builder()
                .walletId(1L)
                .user(user)
                .availableBalance(BigDecimal.ZERO)
                .currency("PHP")
                .build();
    }

    @Test
    void getOrCreateWallet_ReturnsExistingWallet() {
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getOrCreateWallet(1L);

        assertNotNull(result);
        assertEquals(wallet.getWalletId(), result.getWalletId());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void getOrCreateWallet_CreatesNewWallet_WhenNotFound() {
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.getOrCreateWallet(1L);

        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            walletService.createWallet(1L);
        });
    }

    @Test
    void initiateWalletTopup_ThrowsException_WhenAmountIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.initiateWalletTopup(1L, BigDecimal.ZERO, "GCASH");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            walletService.initiateWalletTopup(1L, new BigDecimal("-10"), "GCASH");
        });
    }

    @Test
    void initiateWalletTopup_Success() {
        BigDecimal amount = new BigDecimal("100");
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(amount)
                .build();

        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        when(paymentService.createAndSaveWalletTopupPayment(anyLong(), any(BigDecimal.class), anyString()))
                .thenReturn(payment);

        Payment result = walletService.initiateWalletTopup(1L, amount, "GCASH");

        assertNotNull(result);
        assertEquals(1L, result.getPaymentId());
        verify(walletTransactionRepository, times(2)).save(any(WalletTransaction.class));
    }

    // ===== Additional Comprehensive Tests for Branch Coverage =====

    @Test
    void confirmWalletTopup_Success() {
        wallet.setAvailableBalance(new BigDecimal("0.00"));
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .transactionType("CREDIT")
                .status("PENDING")
                .build();

        Payment payment = Payment.builder()
                .paymentId(1L)
                .amount(new BigDecimal("100.00"))
                .referenceType("WALLET_TOPUP")
                .referenceId(1L)
                .build();

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(payment));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletTransaction result = walletService.confirmWalletTopup(1L, "1", new BigDecimal("100.00"),
                "pay_intent_123");

        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void confirmWalletTopup_InvalidPaymentId_ThrowsException() {
        when(walletRepository.findByUserUserId(999L)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.confirmWalletTopup(999L, "999", new BigDecimal("100.00"), "pay_intent"));
    }

    @Test
    void addToWallet_Success() {
        wallet.setAvailableBalance(new BigDecimal("100.00"));
        BigDecimal addAmount = new BigDecimal("50.00");

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.addToWallet(1L, addAmount);

        assertNotNull(result);
        verify(walletRepository).save(wallet);
    }

    @Test
    void deductFromWallet_Success() {
        wallet.setAvailableBalance(new BigDecimal("100.00"));
        BigDecimal deductAmount = new BigDecimal("30.00");

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.deductFromWallet(1L, deductAmount, "ORDER", 1L);

        assertNotNull(result);
        verify(walletRepository).save(wallet);
    }

    @Test
    void deductFromWallet_InsufficientBalance_ThrowsException() {
        wallet.setAvailableBalance(new BigDecimal("10.00"));
        BigDecimal deductAmount = new BigDecimal("50.00");

        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductFromWallet(1L, deductAmount, "ORDER", 1L));
    }

    @Test
    void getWalletByUserId_Found() {
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        Optional<Wallet> result = walletService.getWalletByUserId(1L);

        assertTrue(result.isPresent());
        assertEquals(wallet.getWalletId(), result.get().getWalletId());
    }

    @Test
    void getWalletByUserId_NotFound() {
        when(walletRepository.findByUserUserId(999L)).thenReturn(Optional.empty());

        Optional<Wallet> result = walletService.getWalletByUserId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getWalletBalance_Success() {
        wallet.setAvailableBalance(new BigDecimal("250.50"));
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        BigDecimal result = walletService.getWalletBalance(1L);

        assertEquals(new BigDecimal("250.50"), result);
    }

    @Test
    void hasEnoughBalance_True() {
        wallet.setAvailableBalance(new BigDecimal("200.00"));
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        boolean result = walletService.hasSufficientBalance(1L, new BigDecimal("150.00"));

        assertTrue(result);
    }

    @Test
    void hasEnoughBalance_False() {
        wallet.setAvailableBalance(new BigDecimal("50.00"));
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));

        boolean result = walletService.hasSufficientBalance(1L, new BigDecimal("100.00"));

        assertFalse(result);
    }

    @Test
    void getWalletTransactionsByWallet_Success() {
        wallet.setWalletId(1L);
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .transactionType("TOPUP")
                .build();
        
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(1L))
                .thenReturn(java.util.Arrays.asList(transaction));

        java.util.List<WalletTransaction> result = walletService.getWalletTransactions(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getWalletTransactionsByWallet_Empty() {
        wallet.setWalletId(1L);
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(1L))
                .thenReturn(java.util.Arrays.asList());

        java.util.List<WalletTransaction> result = walletService.getWalletTransactions(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createWallet_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.createWallet(1L);

        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void initiateWalletTopup_NegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.initiateWalletTopup(1L, new BigDecimal("-50.00"), "CARD"));
    }

    @Test
    void confirmWalletTopup_UpdatesBalance() {
        wallet.setAvailableBalance(new BigDecimal("0.00"));
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(2L)
                .wallet(wallet)
                .amount(new BigDecimal("500.00"))
                .transactionType("TOPUP")
                .status("PENDING")
                .build();

        Payment payment = Payment.builder()
                .paymentId(2L)
                .amount(new BigDecimal("500.00"))
                .referenceType("WALLET_TOPUP")
                .referenceId(2L)
                .build();

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findById(2L)).thenReturn(Optional.of(transaction));
        when(paymentService.getPaymentById(2L)).thenReturn(Optional.of(payment));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletTransaction result = walletService.confirmWalletTopup(1L, "2", new BigDecimal("500.00"),
                "pay_new_intent");

        assertNotNull(result);
    }

    @Test
    void getPendingTransactions_Success() {
        wallet.setWalletId(1L);
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .status("PENDING")
                .build();

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findPendingTransactionsByWallet(1L))
                .thenReturn(java.util.Arrays.asList(transaction));

        java.util.List<WalletTransaction> result = walletService.getPendingTransactions(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getPendingTransactions_Empty() {
        wallet.setWalletId(1L);
        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findPendingTransactionsByWallet(1L))
                .thenReturn(java.util.Arrays.asList());

        java.util.List<WalletTransaction> result = walletService.getPendingTransactions(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTransactionsByReferenceType_Success() {
        wallet.setWalletId(1L);
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .referenceType("ORDER")
                .build();

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByWalletWalletIdAndReferenceType(1L, "ORDER"))
                .thenReturn(java.util.Arrays.asList(transaction));

        java.util.List<WalletTransaction> result = walletService.getTransactionsByReferenceType(1L, "ORDER");

        assertEquals(1, result.size());
    }

    @Test
    void toWalletDTO_Success() {
        wallet.setWalletId(1L);
        wallet.setAvailableBalance(new BigDecimal("100.00"));

        WalletDTO dto = walletService.toWalletDTO(wallet);

        assertNotNull(dto);
        assertEquals(1L, dto.getWalletId());
        assertEquals(1L, dto.getUserId());
        assertEquals(new BigDecimal("100.00"), dto.getAvailableBalance());
        assertEquals("PHP", dto.getCurrency());
    }

    @Test
    void getWalletTransactionById_Found() {
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .build();

        when(walletTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Optional<WalletTransaction> result = walletService.getWalletTransactionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getTransactionId());
    }

    @Test
    void getWalletTransactionById_NotFound() {
        when(walletTransactionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<WalletTransaction> result = walletService.getWalletTransactionById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateTransactionStatus_Success() {
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .status("PENDING")
                .build();

        when(walletTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);

        WalletTransaction result = walletService.updateTransactionStatus(1L, "COMPLETED");

        assertNotNull(result);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void updateTransactionStatus_NotFound() {
        when(walletTransactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.updateTransactionStatus(999L, "COMPLETED"));
    }

    @Test
    void addToWallet_NegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.addToWallet(1L, new BigDecimal("-50.00")));
    }

    @Test
    void deductFromWallet_NegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.deductFromWallet(1L, new BigDecimal("-30.00"), "ORDER", 1L));
    }

    @Test
    void confirmWalletTopup_InvalidReferenceType_ThrowsException() {
        wallet.setAvailableBalance(new BigDecimal("0.00"));
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .build();

        Payment payment = Payment.builder()
                .paymentId(1L)
                .amount(new BigDecimal("100.00"))
                .referenceType("INVALID_TYPE")
                .referenceId(1L)
                .build();

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalArgumentException.class, () -> {
            walletService.confirmWalletTopup(1L, "1", new BigDecimal("100.00"), "pay_intent");
        });
    }

    @Test
    void confirmWalletTopup_NullIntentId_Success() {
        wallet.setAvailableBalance(new BigDecimal("0.00"));
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .transactionType("CREDIT")
                .status("PENDING")
                .build();

        Payment payment = Payment.builder()
                .paymentId(1L)
                .amount(new BigDecimal("100.00"))
                .referenceType("WALLET_TOPUP")
                .referenceId(1L)
                .build();

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(payment));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletTransaction result = walletService.confirmWalletTopup(1L, "1", new BigDecimal("100.00"), null);

        assertNotNull(result);
    }

    @Test
    void confirmWalletTopup_EmptyIntentId_Success() {
        wallet.setAvailableBalance(new BigDecimal("0.00"));
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId(1L)
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .transactionType("CREDIT")
                .status("PENDING")
                .build();

        Payment payment = Payment.builder()
                .paymentId(1L)
                .amount(new BigDecimal("100.00"))
                .referenceType("WALLET_TOPUP")
                .referenceId(1L)
                .build();

        when(walletRepository.findByUserUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(payment));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletTransaction result = walletService.confirmWalletTopup(1L, "1", new BigDecimal("100.00"), "");

        assertNotNull(result);
    }
}
