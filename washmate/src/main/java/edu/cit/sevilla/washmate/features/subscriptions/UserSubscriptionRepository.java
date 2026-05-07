package edu.cit.sevilla.washmate.features.subscriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findByUserUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    Optional<UserSubscription> findFirstByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<UserSubscription> findByUserUserId(Long userId);

    List<UserSubscription> findByStatusAndExpiryDateBefore(String status, LocalDateTime date);
}

