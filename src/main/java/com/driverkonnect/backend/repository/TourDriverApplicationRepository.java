package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourDriverApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourDriverApplicationRepository extends JpaRepository<TourDriverApplication, Long> {
    boolean existsByTourRequest_IdAndDriver_EmailAndIsWithdrawnFalse(Long tourRequestId, String driverEmail);
    List<TourDriverApplication> findByDriver_EmailOrderByAppliedAtDesc(String driverEmail);
    List<TourDriverApplication> findByTourRequest_IdOrderByAppliedAtDesc(Long tourRequestId);
    Optional<TourDriverApplication> findByIdAndDriver_Email(Long id, String driverEmail);
    long countByDriver_EmailAndIsWithdrawnFalse(String driverEmail);
}
