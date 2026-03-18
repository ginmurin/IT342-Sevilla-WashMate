package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerUserId(Long customerId);
    List<Order> findByShopShopId(Long shopId);
    List<Order> findByStatus(String status);
}
