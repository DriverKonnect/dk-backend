package com.driverkonnect.backend.dto.response.driver;

import com.driverkonnect.backend.dto.response.tourcompany.TourLocationResponseDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DriverActiveTourDto {

    private Long tourRequestId;
    private String tourName;
    private String tripType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private Integer nights;
    private Integer paxCount;
    private String travellerNationality;
    private String vehicleTypeName;
    private Integer estimatedKm;
    private String paymentTerm;
    private String specificRequirements;
    private String specialConcerns;
    private String status;
    private BigDecimal perKmRateSnapshot;
    private BigDecimal estimatedEarnings;
    private LocalDateTime assignedAt;
    private List<TourLocationResponseDto> locations;
}
