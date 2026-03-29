package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerUserId(Long customerId);
    // Removed findByShopShopId - single shop system doesn't need shop filtering
    List<Order> findByStatus(String status);
}
