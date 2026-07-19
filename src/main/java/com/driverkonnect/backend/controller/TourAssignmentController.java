package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.tourcompany.TourApplicationDecisionDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourAssignmentResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourDriverApplicationSummaryDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourAssignmentService;
import com.driverkonnect.backend.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tour-company/tours/{tourRequestId}/applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TOUR_COMPANY')")
public class TourAssignmentController {

    private final TourAssignmentService tourAssignmentService;

    @GetMapping
    public ResponseEntity<Response<List<TourDriverApplicationSummaryDto>>> getApplications(
            @PathVariable Long tourRequestId) {
        log.info("Received request to retrieve applications for tour ID: {}", tourRequestId);
        List<TourDriverApplicationSummaryDto> result = tourAssignmentService.getApplicationsForTour(tourRequestId);
        log.info("Successfully retrieved {} applications for tour ID: {}", result.size(), tourRequestId);
        return ResponseUtil.success(result, "Applications retrieved successfully");
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<Response<TourDriverApplicationSummaryDto>> updateStatus(
            @PathVariable Long tourRequestId,
            @PathVariable Long applicationId,
            @Valid @RequestBody TourApplicationDecisionDto request) {
        log.info("Received request to update status of application ID: {} for tour ID: {}",
                applicationId, tourRequestId);
        TourDriverApplicationSummaryDto result =
                tourAssignmentService.updateApplicationStatus(tourRequestId, applicationId, request);
        log.info("Successfully updated status of application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application status updated successfully");
    }

    @PatchMapping("/{applicationId}/accept")
    public ResponseEntity<Response<TourAssignmentResponseDto>> accept(
            @PathVariable Long tourRequestId,
            @PathVariable Long applicationId) {
        log.info("Received request to accept application ID: {} for tour ID: {}",
                applicationId, tourRequestId);
        TourAssignmentResponseDto result =
                tourAssignmentService.acceptApplication(tourRequestId, applicationId);
        log.info("Successfully accepted application ID: {}, created assignment ID: {}",
                applicationId, result.getId());
        return ResponseUtil.created(result, "Driver accepted and assigned successfully");
    }
}
