package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    Optional<User> findByEmailAndIsActiveTrue(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIsActiveTrue(String email);
    boolean existsByUsernameAndIsActiveTrue(String username);
}
