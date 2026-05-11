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
}
