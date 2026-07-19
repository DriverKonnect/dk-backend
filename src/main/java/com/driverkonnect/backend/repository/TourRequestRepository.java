package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourRequest;
import com.driverkonnect.backend.enums.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TourRequestRepository extends JpaRepository<TourRequest, Long> {

    @Query(
        value = """
            SELECT t FROM TourRequest t
            WHERE t.tourCompany.id = :companyId
            AND (:status IS NULL OR t.status = :status)
            AND (:dateFrom IS NULL OR t.startDate >= :dateFrom)
            AND (:dateTo IS NULL OR t.startDate <= :dateTo)
            """,
        countQuery = """
            SELECT COUNT(t) FROM TourRequest t
            WHERE t.tourCompany.id = :companyId
            AND (:status IS NULL OR t.status = :status)
            AND (:dateFrom IS NULL OR t.startDate >= :dateFrom)
            AND (:dateTo IS NULL OR t.startDate <= :dateTo)
            """
    )
    Page<TourRequest> findByCompanyWithFilters(
            @Param("companyId") Long companyId,
            @Param("status") TourStatus status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );

    List<TourRequest> findByStatusOrderByCreatedAtDesc(TourStatus status);
    Optional<TourRequest> findByIdAndTourCompany_Id(Long id, Long tourCompanyId);
}
