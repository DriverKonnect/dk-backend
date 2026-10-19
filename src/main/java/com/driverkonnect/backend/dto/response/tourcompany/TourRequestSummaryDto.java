package com.driverkonnect.backend.dto.response.tourcompany;

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
public class TourRequestSummaryDto {
    private Long id;
    private String referenceNumber;
    private String tourName;
    private String tripType;
    private String travellerNationality;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer paxCount;
    private String vehicleTypeName;
    private String status;
    private String driverFirstName;
    private String driverLastName;
    private Integer rating;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
