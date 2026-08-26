package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.driver.ApplyForTourDto;
import com.driverkonnect.backend.dto.response.driver.DriverActiveTourDto;
import com.driverkonnect.backend.dto.response.driver.DriverHistorySummaryDto;
import com.driverkonnect.backend.dto.response.driver.TourApplicationResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;

import java.util.List;

public interface TourApplicationService {
    List<TourRequestSummaryDto> getPublishedTours();
    TourRequestResponseDto getPublishedTourById(Long tourRequestId);
    TourApplicationResponseDto apply(Long tourRequestId, ApplyForTourDto dto);
    List<TourApplicationResponseDto> getMyApplications();
    TourApplicationResponseDto withdraw(Long applicationId);
    DriverActiveTourDto getMyActiveTour();
    DriverActiveTourDto startTour(Long tourRequestId);
    DriverHistorySummaryDto getMyHistory(int page, int size);
}
