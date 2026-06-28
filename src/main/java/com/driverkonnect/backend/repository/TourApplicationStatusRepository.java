package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourApplicationStatusRepository extends JpaRepository<TourApplicationStatus, Long> {
    boolean existsByLabelIgnoreCase(String label);
    List<TourApplicationStatus> findAllByOrderByLabelAsc();
}
