package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    /**
     * Find all transactions for a specific wallet.
     */
    List<WalletTransaction> findByWalletWalletId(Long walletId);

    /**
     * Find wallet transactions by polymorphic reference (e.g., ORDER, PAYMENT, SUBSCRIPTION).
     */
    List<WalletTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    /**
     * Find wallet transactions by status and wallet ID.
     */
    List<WalletTransaction> findByWalletWalletIdAndStatus(Long walletId, String status);

    /**
     * Find wallet transactions by transaction type and wallet ID.
     */
    List<WalletTransaction> findByWalletWalletIdAndTransactionType(Long walletId, String transactionType);

    /**
     * Find wallet transactions ordered by creation date (newest first).
     */
    List<WalletTransaction> findByWalletWalletIdOrderByCreatedAtDesc(Long walletId);

    /**
     * Find pending transactions for a wallet.
     */
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.walletId = :walletId AND wt.status = 'PENDING'")
    List<WalletTransaction> findPendingTransactionsByWallet(@Param("walletId") Long walletId);

    /**
     * Find the latest transaction for a wallet.
     */
    Optional<WalletTransaction> findTopByWalletWalletIdOrderByCreatedAtDesc(Long walletId);

    /**
     * Find transactions by reference type for a specific wallet.
     */
    List<WalletTransaction> findByWalletWalletIdAndReferenceType(Long walletId, String referenceType);
}