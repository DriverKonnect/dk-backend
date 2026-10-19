package com.driverkonnect.backend.dto.response.driver;

import com.driverkonnect.backend.dto.response.tourcompany.TourLocationResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private BigDecimal estimatedKm;
    private String paymentTerm;
    private String specificRequirements;
    private String specialConcerns;
    private String status;
    private BigDecimal perKmRateSnapshot;
    private BigDecimal estimatedEarnings;
    private LocalDateTime assignedAt;
    private List<TourLocationResponseDto> locations;
}
