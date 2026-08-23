package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourAssignment;
import com.driverkonnect.backend.enums.TourStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TourAssignmentRepository extends JpaRepository<TourAssignment, Long> {
    boolean existsByTourRequest_Id(Long tourRequestId);
    Optional<TourAssignment> findByTourRequest_Id(Long tourRequestId);
    List<TourAssignment> findByTourRequest_IdIn(List<Long> tourRequestIds);

    @Query("SELECT a FROM TourAssignment a WHERE a.driver.email = :email AND a.tourRequest.status IN :statuses")
    Optional<TourAssignment> findActiveByDriverEmail(
            @Param("email") String email,
            @Param("statuses") List<TourStatus> statuses);

    Optional<TourAssignment> findByDriver_EmailAndTourRequest_Id(String email, Long tourRequestId);

    List<TourAssignment> findByDriver_EmailAndTourRequest_StatusInOrderByAssignedAtDesc(
            String email, List<TourStatus> statuses);
}
