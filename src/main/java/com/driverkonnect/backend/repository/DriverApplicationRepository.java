package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.DriverApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverApplicationRepository extends JpaRepository<DriverApplication, Long> {
    boolean existsByNicNumber(String nicNumber);
    Optional<DriverApplication> findByUser_Email(String email);
}
