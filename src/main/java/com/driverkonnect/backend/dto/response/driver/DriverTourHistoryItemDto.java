package com.driverkonnect.backend.dto.response.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverTourHistoryItemDto {

    private Long tourRequestId;
    private String tourName;
    private String tripType;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal estimatedEarnings;
    private Integer rating;
}
