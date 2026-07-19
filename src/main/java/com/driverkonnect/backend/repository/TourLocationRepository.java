package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourLocationRepository extends JpaRepository<TourLocation, Long> {
    List<TourLocation> findByTourRequestIdOrderBySequenceOrderAsc(Long tourRequestId);
}
