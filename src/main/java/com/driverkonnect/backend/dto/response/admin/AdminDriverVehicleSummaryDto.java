package com.driverkonnect.backend.dto.response.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminDriverVehicleSummaryDto {

    private Long id;
    private Long driverId;
    private String driverFirstName;
    private String driverLastName;
    private String driverEmail;
    private String brand;
    private String model;
    private Integer year;
    private Integer mileageKm;
    private String vehicleCategory;
    private BigDecimal perKmRate;
    private String approvalStatus;
    private String rejectionReason;
    private String photoFrontUrl;
    private String photoBackUrl;
    private String photoSideUrl;
    private String photoInteriorUrl;
    private String vehicleLicenceUrl;
    private String insuranceUrl;
    private LocalDate insuranceExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
