package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.WalletDTO;
import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.Wallet;
import edu.cit.sevilla.washmate.entity.WalletTransaction;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.repository.WalletRepository;
import edu.cit.sevilla.washmate.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    // ===== WALLET CREATION AND INITIALIZATION =====

    /**
     * Get or create wallet for user.
     */
    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserUserId(userId)
                .orElseGet(() -> createWallet(userId));
    }

    /**
     * Create initial wallet for user.
     */
    @Transactional
    public Wallet createWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Wallet wallet = Wallet.builder()
                .user(user)
                .availableBalance(BigDecimal.ZERO)
                .currency("PHP")
                .build();

        return walletRepository.save(wallet);
    }

    // ===== WALLET TOP-UP WITH POLYMORPHIC PAYMENTS =====

    /**
     * Initiate wallet top-up by creating polymorphic payment.
     */
    @Transactional
    public Payment initiateWalletTopup(Long userId, BigDecimal amount, String paymentMethod) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }

        // Ensure wallet exists
        Wallet wallet = getOrCreateWallet(userId);

        // Create WalletTransaction record first (as placeholder)
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .transactionType("CREDIT")
                .status("PENDING")
                .description("Wallet top-up via " + paymentMethod)
                .balanceBefore(wallet.getAvailableBalance())
                .balanceAfter(wallet.getAvailableBalance().add(amount))
                .build();

        transaction = walletTransactionRepository.save(transaction);

        // Create polymorphic payment referencing the wallet transaction
        return paymentService.createAndSaveWalletTopupPayment(
                transaction.getTransactionId(),
                amount,
                paymentMethod
        );
    }

    /**
     * Confirm wallet top-up and update balance.
     */
    @Transactional
    public WalletTransaction confirmWalletTopup(Long userId, String paymentId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);

        // Find polymorphic payment by PayMongo ID
        Payment payment = paymentService.getPaymentByPaymongoIntentId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!"WALLET_TOPUP".equals(payment.getReferenceType())) {
            throw new IllegalArgumentException("Payment is not for wallet top-up");
        }

        // Get the wallet transaction
        WalletTransaction transaction = walletTransactionRepository.findById(payment.getReferenceId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet transaction not found"));

        // Validate amount matches
        if (amount.compareTo(payment.getAmount()) != 0) {
            throw new IllegalArgumentException("Amount mismatch");
        }

        // Update wallet balance
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        walletRepository.save(wallet);

        // Update transaction status and final balance
        transaction.setStatus("COMPLETED");
        transaction.setBalanceAfter(wallet.getAvailableBalance());

        // Update payment status
        payment.setPaymentStatus("COMPLETED");
        payment.setPaymentDate(LocalDateTime.now());
        paymentService.savePayment(payment);

        return walletTransactionRepository.save(transaction);
    }

    // ===== WALLET BALANCE MANAGEMENT =====

    /**
     * Add money to wallet (direct balance addition).
     */
    @Transactional
    public Wallet addToWallet(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = getOrCreateWallet(userId);
        BigDecimal oldBalance = wallet.getAvailableBalance();
        BigDecimal newBalance = oldBalance.add(amount);

        wallet.setAvailableBalance(newBalance);
        wallet = walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .transactionType("CREDIT")
                .referenceType("MANUAL_CREDIT")
                .status("COMPLETED")
                .description("Manual wallet credit")
                .balanceBefore(oldBalance)
                .balanceAfter(newBalance)
                .build();

        walletTransactionRepository.save(transaction);

        return wallet;
    }

    /**
     * Deduct money from wallet for payments.
     */
    @Transactional
    public Wallet deductFromWallet(Long userId, BigDecimal amount, String referenceType, Long referenceId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = getOrCreateWallet(userId);

        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient wallet balance");
        }

        BigDecimal oldBalance = wallet.getAvailableBalance();
        BigDecimal newBalance = oldBalance.subtract(amount);

        wallet.setAvailableBalance(newBalance);
        wallet = walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .transactionType("DEDUCTION")
                .referenceType(referenceType)
                .referenceId(referenceId)
                .status("COMPLETED")
                .description("Payment deduction for " + referenceType)
                .balanceBefore(oldBalance)
                .balanceAfter(newBalance)
                .build();

        walletTransactionRepository.save(transaction);

        return wallet;
    }

    /**
     * Get wallet balance for user.
     */
    public BigDecimal getWalletBalance(Long userId) {
        return getOrCreateWallet(userId).getAvailableBalance();
    }

    /**
     * Get wallet for user.
     */
    public Optional<Wallet> getWalletByUserId(Long userId) {
        return walletRepository.findByUserUserId(userId);
    }

    // ===== WALLET TRANSACTION QUERIES =====

    /**
     * Get wallet transactions for user.
     */
    public List<WalletTransaction> getWalletTransactions(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return walletTransactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(wallet.getWalletId());
    }

    /**
     * Get pending wallet transactions.
     */
    public List<WalletTransaction> getPendingTransactions(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return walletTransactionRepository.findPendingTransactionsByWallet(wallet.getWalletId());
    }

    /**
     * Get wallet transactions by reference type.
     */
    public List<WalletTransaction> getTransactionsByReferenceType(Long userId, String referenceType) {
        Wallet wallet = getOrCreateWallet(userId);
        return walletTransactionRepository.findByWalletWalletIdAndReferenceType(
                wallet.getWalletId(), referenceType);
    }

    // ===== DTO CONVERSION =====

    /**
     * Convert Wallet entity to DTO.
     */
    public WalletDTO toWalletDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setWalletId(wallet.getWalletId());
        dto.setUserId(wallet.getUser().getUserId());
        dto.setAvailableBalance(wallet.getAvailableBalance());
        dto.setCurrency(wallet.getCurrency());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }

    // ===== WALLET VALIDATION =====

    /**
     * Check if user has sufficient wallet balance.
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        BigDecimal currentBalance = getWalletBalance(userId);
        return currentBalance.compareTo(requiredAmount) >= 0;
    }

    /**
     * Get wallet transaction by ID.
     */
    public Optional<WalletTransaction> getWalletTransactionById(Long transactionId) {
        return walletTransactionRepository.findById(transactionId);
    }

    /**
     * Update wallet transaction status.
     */
    @Transactional
    public WalletTransaction updateTransactionStatus(Long transactionId, String newStatus) {
        WalletTransaction transaction = walletTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        transaction.setStatus(newStatus);
        return walletTransactionRepository.save(transaction);
    }
}