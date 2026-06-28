package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<VehicleType> findAllByOrderByNameAsc();
}
