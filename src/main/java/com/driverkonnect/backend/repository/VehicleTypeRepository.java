package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {
    Optional<VehicleType> findByNameIgnoreCase(String name);
    List<VehicleType> findAllByOrderByNameAsc();
}
