package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourApplicationStatusRepository extends JpaRepository<TourApplicationStatus, Long> {
    boolean existsByCodeIgnoreCase(String code);
    List<TourApplicationStatus> findAllByOrderByLabelAsc();
}
