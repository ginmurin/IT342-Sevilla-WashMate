package edu.cit.sevilla.washmate.repository;

import edu.cit.sevilla.washmate.entity.ServiceVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceVariantRepository extends JpaRepository<ServiceVariant, Long> {
    List<ServiceVariant> findByService_ServiceIdAndIsActiveTrue(Long serviceId);
}
