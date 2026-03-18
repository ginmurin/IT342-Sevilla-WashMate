package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByShopShopId(Long shopId);
    List<Feedback> findByCustomerUserId(Long customerId);
    List<Feedback> findByOrderOrderId(Long orderId);
}
