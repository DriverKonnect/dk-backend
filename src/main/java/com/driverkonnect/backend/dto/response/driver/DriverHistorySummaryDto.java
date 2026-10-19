package com.driverkonnect.backend.dto.response.driver;

import com.driverkonnect.backend.generics.PagedResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverHistorySummaryDto {

    private long completedCount;
    private Double averageRating;
    private BigDecimal lifetimeEarned;
    private PagedResponseDto<DriverTourHistoryItemDto> tours;
}
