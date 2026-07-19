package com.driverkonnect.backend.dto.response.tourcompany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String note;
    private String statusLabel;
    private Boolean isWithdrawn;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
