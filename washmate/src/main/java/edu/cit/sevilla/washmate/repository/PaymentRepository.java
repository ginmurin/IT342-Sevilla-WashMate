package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderOrderId(Long orderId);
    Optional<Payment> findByPaymongoPaymentIntentId(String paymongoPaymentIntentId);
}
