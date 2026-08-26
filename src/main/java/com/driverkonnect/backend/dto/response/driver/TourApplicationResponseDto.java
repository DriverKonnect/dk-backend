package com.driverkonnect.backend.dto.response.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourApplicationResponseDto {
    private Long id;
    private Long tourRequestId;
    private String tourName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String vehicleTypeName;
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
