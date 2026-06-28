package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourRequest;
import com.driverkonnect.backend.enums.TourStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourRequestRepository extends JpaRepository<TourRequest, Long> {
    List<TourRequest> findByTourCompanyIdOrderByCreatedAtDesc(Long tourCompanyId);
    List<TourRequest> findByStatusOrderByCreatedAtDesc(TourStatus status);
}
