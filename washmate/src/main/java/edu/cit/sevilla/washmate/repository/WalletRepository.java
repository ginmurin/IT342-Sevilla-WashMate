package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserUserId(Long userId);
}
