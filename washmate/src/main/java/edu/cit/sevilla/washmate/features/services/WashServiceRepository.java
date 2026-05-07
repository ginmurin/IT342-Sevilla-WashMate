package edu.cit.sevilla.washmate.features.services;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WashServiceRepository extends JpaRepository<WashService, Long> {
    List<WashService> findByIsActiveTrue();
}

