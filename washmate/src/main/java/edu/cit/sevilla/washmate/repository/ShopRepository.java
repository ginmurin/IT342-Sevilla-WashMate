package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByOwnerUserId(Long ownerId);
    List<Shop> findByIsActiveTrue();
}
