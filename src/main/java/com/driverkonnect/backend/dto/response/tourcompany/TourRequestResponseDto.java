package com.driverkonnect.backend.dto.response.tourcompany;

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
public class TourRequestResponseDto {
    private Long id;
    private String tourName;
    private String tripType;
    private String travellerNationality;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private Integer nights;
    private Integer paxCount;
    private Long vehicleTypeId;
    private String vehicleTypeName;
    private BigDecimal estimatedKm;
    private String specificRequirements;
    private String specialConcerns;
    private String paymentTerm;
    private String status;
    private List<TourLocationResponseDto> locations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
