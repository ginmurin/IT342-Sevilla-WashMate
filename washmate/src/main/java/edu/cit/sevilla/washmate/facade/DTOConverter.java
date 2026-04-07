package edu.cit.sevilla.washmate.facade;

import edu.cit.sevilla.washmate.dto.*;
import edu.cit.sevilla.washmate.entity.*;
import edu.cit.sevilla.washmate.service.OrderService;
import edu.cit.sevilla.washmate.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Facade for centralizing DTO conversion logic across the application.
 *
 * Previously scattered conversion logic is now consolidated in this single component.
 * Benefits:
 * - Single source of truth for all DTO conversions
 * - Easier maintenance and testing
 * - Consistent conversion patterns across application
 * - Delegates to services for complex conversion logic
 *
 * Usage: Inject this component in controllers/services needing DTO conversions.
 */
@Component
@RequiredArgsConstructor
public class DTOConverter {

    private final OrderService orderService;
    private final SubscriptionService subscriptionService;

    // ===== ORDER DTO CONVERSIONS =====

    /**
     * Convert Order entity to OrderDTO.
     * Delegates to OrderService which contains full conversion logic.
     */
    public OrderDTO toOrderDTO(Order order) {
        return orderService.toOrderDTO(order);
    }

    /**
     * Convert list of Order entities to list of OrderDTOs.
     */
    public List<OrderDTO> toOrderDTOs(List<Order> orders) {
        return orderService.toOrderDTOs(orders);
    }

    // ===== PAYMENT DTO CONVERSIONS =====

    /**
     * Convert Payment entity to PaymentDTO.
     * Delegates to OrderService which contains the conversion logic.
     */
    public PaymentDTO toPaymentDTO(Payment payment) {
        return orderService.toPaymentDTO(payment);
    }

    /**
     * Convert list of Payment entities to list of PaymentDTOs.
     */
    public List<PaymentDTO> toPaymentDTOs(List<Payment> payments) {
        return payments.stream()
                .map(this::toPaymentDTO)
                .collect(Collectors.toList());
    }

    // ===== SUBSCRIPTION DTO CONVERSIONS =====

    /**
     * Convert SubscriptionDTO using SubscriptionService.
     */
    public SubscriptionDTO getSubscriptionDTO(Subscription subscription) {
        return subscriptionService.getSubscriptionDTO(subscription).orElse(null);
    }

    /**
     * Convert UserSubscription entity to UserSubscriptionDTO.
     * Delegates to SubscriptionService which contains the conversion logic.
     */
    public UserSubscriptionDTO toUserSubscriptionDTO(UserSubscription userSubscription) {
        return subscriptionService.toUserSubscriptionDTO(userSubscription);
    }

    /**
     * Convert list of UserSubscription entities to list of UserSubscriptionDTOs.
     */
    public List<UserSubscriptionDTO> toUserSubscriptionDTOs(List<UserSubscription> userSubscriptions) {
        return userSubscriptions.stream()
                .map(this::toUserSubscriptionDTO)
                .collect(Collectors.toList());
    }

    // ===== WALLET DTO CONVERSIONS =====

    /**
     * Convert Wallet entity to WalletDTO.
     * Can be extended with custom wallet conversion logic as needed.
     */
    public WalletDTO toWalletDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        if (wallet != null) {
            dto.setWalletId(wallet.getWalletId());
            dto.setUserId(wallet.getUser().getUserId());
            dto.setBalance(wallet.getBalance());
            dto.setCreatedAt(wallet.getCreatedAt());
            dto.setUpdatedAt(wallet.getUpdatedAt());
        }
        return dto;
    }

    /**
     * Convert WalletTransaction entity to WalletTransactionDTO.
     */
    public WalletTransactionDTO toWalletTransactionDTO(WalletTransaction transaction) {
        WalletTransactionDTO dto = new WalletTransactionDTO();
        if (transaction != null) {
            dto.setTransactionId(transaction.getTransactionId());
            dto.setWalletId(transaction.getWallet().getWalletId());
            dto.setTransactionType(transaction.getTransactionType());
            dto.setAmount(transaction.getAmount());
            dto.setStatus(transaction.getStatus());
            dto.setReferenceType(transaction.getReferenceType());
            dto.setReferenceId(transaction.getReferenceId());
            dto.setCreatedAt(transaction.getCreatedAt());
        }
        return dto;
    }

    /**
     * Convert list of WalletTransaction entities to list of WalletTransactionDTOs.
     */
    public List<WalletTransactionDTO> toWalletTransactionDTOs(List<WalletTransaction> transactions) {
        return transactions.stream()
                .map(this::toWalletTransactionDTO)
                .collect(Collectors.toList());
    }
}
