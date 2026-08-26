package com.driverkonnect.backend.dto.response.driver;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
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
