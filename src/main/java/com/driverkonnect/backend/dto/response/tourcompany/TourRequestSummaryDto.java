package com.driverkonnect.backend.dto.response.tourcompany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourRequestSummaryDto {
    private Long id;
    private String tourName;
    private String tripType;
    private String travellerNationality;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer paxCount;
    private String vehicleTypeName;
    private String status;
    private LocalDateTime createdAt;
}
