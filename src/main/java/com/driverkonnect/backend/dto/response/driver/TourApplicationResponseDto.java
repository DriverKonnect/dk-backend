package com.driverkonnect.backend.dto.response.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourApplicationResponseDto {
    private Long id;
    private Long tourRequestId;
    private String tourName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String vehicleTypeName;
    private String note;
    private String statusLabel;
    private Boolean isWithdrawn;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
