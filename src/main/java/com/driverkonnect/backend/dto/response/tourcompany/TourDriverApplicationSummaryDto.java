package com.driverkonnect.backend.dto.response.tourcompany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourDriverApplicationSummaryDto {
    private Long id;
    private Long driverId;
    private String driverFirstName;
    private String driverLastName;
    private String driverEmail;
    private Long vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleCategory;
    private BigDecimal perKmRateSnapshot;
    private String note;
    private String statusLabel;
    private Boolean isWithdrawn;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
