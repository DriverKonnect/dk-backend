package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TourAssignmentRepository extends JpaRepository<TourAssignment, Long> {
    boolean existsByTourRequest_Id(Long tourRequestId);
    Optional<TourAssignment> findByTourRequest_Id(Long tourRequestId);
}
