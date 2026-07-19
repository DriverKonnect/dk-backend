package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.tourcompany.TourApplicationDecisionDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourAssignmentResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourDriverApplicationSummaryDto;

import java.util.List;

public interface TourAssignmentService {
    List<TourDriverApplicationSummaryDto> getApplicationsForTour(Long tourRequestId);
    TourDriverApplicationSummaryDto updateApplicationStatus(Long tourRequestId, Long applicationId, TourApplicationDecisionDto dto);
    TourAssignmentResponseDto acceptApplication(Long tourRequestId, Long applicationId);
}
