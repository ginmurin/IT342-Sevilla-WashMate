package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserUserIdAndIsReadFalse(Long userId);
    long countByUserUserIdAndIsReadFalse(Long userId);
}
