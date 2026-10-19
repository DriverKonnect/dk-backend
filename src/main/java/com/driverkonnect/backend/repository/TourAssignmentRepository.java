package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourAssignment;
import com.driverkonnect.backend.enums.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    Page<TourAssignment> findByDriver_EmailAndTourRequest_StatusInOrderByAssignedAtDesc(
            String email, List<TourStatus> statuses, Pageable pageable);

    List<TourAssignment> findByDriver_EmailAndTourRequest_Status(String email, TourStatus status);

    long countByDriver_Email(String email);

    @Query("SELECT AVG(a.rating) FROM TourAssignment a WHERE a.driver.email = :email AND a.rating IS NOT NULL")
    Double getAverageRatingByDriverEmail(@Param("email") String email);

    @Query("""
            SELECT COUNT(a) FROM TourAssignment a
            WHERE a.driver.email = :email
            AND a.tourRequest.startDate BETWEEN :weekStart AND :weekEnd
            """)
    long countAssignmentsInWeek(
            @Param("email") String email,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);
}
