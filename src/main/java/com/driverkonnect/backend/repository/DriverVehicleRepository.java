package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.DriverVehicle;
import com.driverkonnect.backend.enums.VehicleApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverVehicleRepository extends JpaRepository<DriverVehicle, Long> {

    List<DriverVehicle> findByDriver_EmailAndIsActiveTrueOrderByCreatedAtDesc(String email);

    Optional<DriverVehicle> findByIdAndDriver_EmailAndIsActiveTrue(Long id, String email);

    List<DriverVehicle> findByDriver_EmailAndApprovalStatusAndIsActiveTrue(String email, VehicleApprovalStatus approvalStatus);

    @Query("""
            SELECT v FROM DriverVehicle v
            WHERE v.isActive = true
            AND (:status IS NULL OR v.approvalStatus = :status)
            ORDER BY v.createdAt DESC
            """)
    Page<DriverVehicle> findAllActiveWithFilter(@Param("status") VehicleApprovalStatus status, Pageable pageable);
}
