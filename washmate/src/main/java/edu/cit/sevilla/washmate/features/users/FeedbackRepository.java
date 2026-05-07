package edu.cit.sevilla.washmate.features.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByCustomerUserId(Long customerId);
    List<Feedback> findByOrderOrderId(Long orderId);
}

