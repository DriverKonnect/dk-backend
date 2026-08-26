package com.driverkonnect.backend.dto.response.driver;

import com.driverkonnect.backend.generics.PagedResponseDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriverHistorySummaryDto {

    private long completedCount;
    private Double averageRating;
    private BigDecimal lifetimeEarned;
    private PagedResponseDto<DriverTourHistoryItemDto> tours;
}
