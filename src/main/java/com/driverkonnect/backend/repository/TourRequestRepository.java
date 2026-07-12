package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourRequest;
import com.driverkonnect.backend.enums.TourStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourRequestRepository extends JpaRepository<TourRequest, Long> {
    List<TourRequest> findByTourCompany_IdOrderByCreatedAtDesc(Long tourCompanyId);
    List<TourRequest> findByStatusOrderByCreatedAtDesc(TourStatus status);
    Optional<TourRequest> findByIdAndTourCompany_Id(Long id, Long tourCompanyId);
}
