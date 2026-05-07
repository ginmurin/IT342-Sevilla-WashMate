package edu.cit.sevilla.washmate.features.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByOauthId(String oauthId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}

