package edu.cit.sevilla.washmate.controller;

import edu.cit.sevilla.washmate.dto.PaymentDTO;
import edu.cit.sevilla.washmate.dto.WalletDTO;
import edu.cit.sevilla.washmate.dto.WalletTransactionDTO;
import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.Wallet;
import edu.cit.sevilla.washmate.entity.WalletTransaction;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for wallet operations with polymorphic payment integration.
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    // ===== WALLET BALANCE OPERATIONS =====

    /**
     * Get current user's wallet balance.
     */
    @GetMapping("/balance")
    public ResponseEntity<WalletDTO> getWalletBalance(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletService.getOrCreateWallet(user.getUserId());
        return ResponseEntity.ok(walletService.toWalletDTO(wallet));
    }

    /**
     * Check if user has sufficient balance for a transaction.
     */
    @GetMapping("/balance/check")
    public ResponseEntity<Map<String, Object>> checkSufficientBalance(
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasSufficient = walletService.hasSufficientBalance(user.getUserId(), amount);
        BigDecimal currentBalance = walletService.getWalletBalance(user.getUserId());

        return ResponseEntity.ok(Map.of(
                "hasSufficientBalance", hasSufficient,
                "currentBalance", currentBalance,
                "requiredAmount", amount
        ));
    }

    // ===== WALLET TOP-UP WITH POLYMORPHIC PAYMENTS =====

    /**
     * Initiate wallet top-up using polymorphic payment pattern.
     */
    @PostMapping("/topup/initiate")
    public ResponseEntity<PaymentDTO> initiateWalletTopup(
            @Validated @RequestBody WalletTopupRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request == null || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }

        // Normalize payment method to uppercase
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CARD";

        Payment payment = walletService.initiateWalletTopup(
                user.getUserId(),
                request.getAmount(),
                paymentMethod
        );

        return ResponseEntity.ok(toPaymentDTO(payment));
    }

    /**
     * Confirm wallet top-up and update balance.
     */
    @PostMapping("/topup/confirm/{paymentId}")
    public ResponseEntity<WalletDTO> confirmWalletTopup(
            @PathVariable String paymentId,
            @RequestBody(required = false) Map<String, Object> request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get amount from request or from payment record
        BigDecimal amount = null;
        if (request != null && request.containsKey("amount")) {
            amount = new BigDecimal(request.get("amount").toString());
        }

        // Get PayMongo payment intent ID if provided
        String paymongoPaymentIntentId = null;
        if (request != null && request.containsKey("paymongoPaymentIntentId")) {
            Object paymongo = request.get("paymongoPaymentIntentId");
            if (paymongo != null && !paymongo.toString().isEmpty()) {
                paymongoPaymentIntentId = paymongo.toString();
            }
        }

        try {
            System.out.println("DEBUG: Confirming wallet top-up for paymentId=" + paymentId + ", amount=" + amount + ", paymongoPaymentIntentId=" + paymongoPaymentIntentId);
            System.out.println("DEBUG: Full request body: " + request);
            WalletTransaction transaction = walletService.confirmWalletTopup(user.getUserId(), paymentId, amount, paymongoPaymentIntentId);
            Wallet wallet = walletService.getOrCreateWallet(user.getUserId());
            System.out.println("DEBUG: Wallet after confirmation - balance=" + wallet.getAvailableBalance());
            return ResponseEntity.ok(walletService.toWalletDTO(wallet));
        } catch (Exception e) {
            System.out.println("ERROR confirming wallet top-up: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to confirm wallet top-up: " + e.getMessage());
        }
    }

    // ===== WALLET TRANSACTION MANAGEMENT =====

    /**
     * Get current user's wallet transactions.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionDTO>> getWalletTransactions(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WalletTransaction> transactions = walletService.getWalletTransactions(user.getUserId());
        List<WalletTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::toWalletTransactionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDTOs);
    }

    /**
     * Get pending wallet transactions for current user.
     */
    @GetMapping("/transactions/pending")
    public ResponseEntity<List<WalletTransactionDTO>> getPendingTransactions(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WalletTransaction> transactions = walletService.getPendingTransactions(user.getUserId());
        List<WalletTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::toWalletTransactionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDTOs);
    }

    /**
     * Get transactions by reference type.
     */
    @GetMapping("/transactions/type/{referenceType}")
    public ResponseEntity<List<WalletTransactionDTO>> getTransactionsByType(
            @PathVariable String referenceType,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WalletTransaction> transactions = walletService.getTransactionsByReferenceType(
                user.getUserId(), referenceType);

        List<WalletTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::toWalletTransactionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDTOs);
    }

    // ===== MANUAL WALLET OPERATIONS (Admin/Debug) =====

    /**
     * Add money to wallet (direct credit).
     */
    @PostMapping("/credit")
    public ResponseEntity<WalletDTO> addToWallet(
            @RequestBody Map<String, BigDecimal> request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal amount = request.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = walletService.addToWallet(user.getUserId(), amount);
        return ResponseEntity.ok(walletService.toWalletDTO(wallet));
    }

    /**
     * Deduct money from wallet.
     */
    @PostMapping("/debit")
    public ResponseEntity<WalletDTO> deductFromWallet(
            @RequestBody WalletDebitRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = walletService.deductFromWallet(
                user.getUserId(),
                request.getAmount(),
                request.getReferenceType(),
                request.getReferenceId()
        );

        return ResponseEntity.ok(walletService.toWalletDTO(wallet));
    }

    // ===== DTO CONVERSION METHODS =====

    /**
     * Convert Payment entity to DTO for wallet payments.
     */
    private PaymentDTO toPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymongoPaymentIntentId(payment.getPaymongoPaymentIntentId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());

        // For polymorphic payments, populate walletTransactionId if it's a wallet payment
        if ("WALLET_TOPUP".equals(payment.getReferenceType())) {
            dto.setReferenceId(payment.getReferenceId());
            dto.setReferenceType(payment.getReferenceType());
        }

        return dto;
    }

    /**
     * Convert WalletTransaction entity to DTO.
     */
    private WalletTransactionDTO toWalletTransactionDTO(WalletTransaction transaction) {
        WalletTransactionDTO dto = new WalletTransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setWalletId(transaction.getWallet().getWalletId());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setReferenceType(transaction.getReferenceType());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setStatus(transaction.getStatus());
        dto.setDescription(transaction.getDescription());
        dto.setBalanceBefore(transaction.getBalanceBefore());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }

    // ===== REQUEST CLASSES =====

    public static class WalletTopupRequest {
        private BigDecimal amount;
        private String paymentMethod = "CARD";

        // Default constructor for JSON deserialization
        public WalletTopupRequest() {}

        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class WalletDebitRequest {
        private BigDecimal amount;
        private String referenceType;
        private Long referenceId;

        // Default constructor for JSON deserialization
        public WalletDebitRequest() {}

        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getReferenceType() { return referenceType; }
        public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
        public Long getReferenceId() { return referenceId; }
        public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    }
}