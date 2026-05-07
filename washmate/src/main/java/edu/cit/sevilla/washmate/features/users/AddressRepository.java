package edu.cit.sevilla.washmate.features.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserUserId(Long userId);
    Optional<Address> findByUserUserIdAndIsDefaultTrue(Long userId);
}

