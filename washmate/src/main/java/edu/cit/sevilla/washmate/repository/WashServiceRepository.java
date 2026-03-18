package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.WashService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WashServiceRepository extends JpaRepository<WashService, Long> {
    List<WashService> findByIsActiveTrue();
}
