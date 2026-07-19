package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourAssignmentRepository extends JpaRepository<TourAssignment, Long> {
    boolean existsByTourRequest_Id(Long tourRequestId);
    Optional<TourAssignment> findByTourRequest_Id(Long tourRequestId);
    List<TourAssignment> findByTourRequest_IdIn(List<Long> tourRequestIds);
}
