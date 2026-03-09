package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserUserId(Long userId);
    Optional<Subscription> findByUserUserIdAndStatus(Long userId, String status);
}
