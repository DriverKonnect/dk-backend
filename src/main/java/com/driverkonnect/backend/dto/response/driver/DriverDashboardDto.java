package com.driverkonnect.backend.dto.response.driver;

import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverDashboardDto {

    private String firstName;
    private String lastName;
    private Boolean isVerified;
    private Double rating;
    private long weeklyToursCount;
    private Double acceptanceRate;
    private Double onTimeRate;
    private List<TourRequestSummaryDto> newTourRequests;
}
