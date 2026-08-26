package com.driverkonnect.backend.entity;

import com.driverkonnect.backend.enums.VehicleApprovalStatus;
import com.driverkonnect.backend.enums.VehicleCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "driver_vehicles")
public class DriverVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "mileage_km", nullable = false)
    private Integer mileageKm;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_category", nullable = false, length = 50)
    private VehicleCategory vehicleCategory;

    @Column(name = "per_km_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal perKmRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private VehicleApprovalStatus approvalStatus;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "photo_front_url", length = 500)
    private String photoFrontUrl;

    @Column(name = "photo_back_url", length = 500)
    private String photoBackUrl;

    @Column(name = "photo_side_url", length = 500)
    private String photoSideUrl;

    @Column(name = "photo_interior_url", length = 500)
    private String photoInteriorUrl;

    @Column(name = "vehicle_licence_url", length = 500)
    private String vehicleLicenceUrl;

    @Column(name = "insurance_url", length = 500)
    private String insuranceUrl;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
