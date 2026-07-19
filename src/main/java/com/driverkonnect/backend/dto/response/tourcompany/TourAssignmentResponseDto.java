package com.driverkonnect.backend.dto.response.tourcompany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourAssignmentResponseDto {
    private Long id;
    private Long tourRequestId;
    private String tourName;
    private Long driverId;
    private String driverFirstName;
    private String driverLastName;
    private String driverEmail;
    private LocalDateTime assignedAt;
}
