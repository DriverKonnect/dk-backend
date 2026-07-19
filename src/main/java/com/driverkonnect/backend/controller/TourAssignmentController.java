package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.tourcompany.TourApplicationDecisionDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourAssignmentResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourDriverApplicationSummaryDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourAssignmentService;
import com.driverkonnect.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tour Company - Driver Applications", description = "Review and manage driver applications received for a published tour. Tour companies can assign status labels and formally accept a driver, which locks in the assignment.")
public class TourAssignmentController {

    private final TourAssignmentService tourAssignmentService;

    @GetMapping
    @Operation(
            summary = "List applications for a tour",
            description = "Returns all driver applications for the specified tour, ordered by application date. Includes driver details, optional note, current status label, and withdrawal state. Returns 404 if the tour does not belong to the authenticated tour company."
    )
    public ResponseEntity<Response<List<TourDriverApplicationSummaryDto>>> getApplications(
            @Parameter(description = "Tour request ID") @PathVariable Long tourRequestId) {
        log.info("Received request to retrieve applications for tour ID: {}", tourRequestId);
        List<TourDriverApplicationSummaryDto> result = tourAssignmentService.getApplicationsForTour(tourRequestId);
        log.info("Successfully retrieved {} applications for tour ID: {}", result.size(), tourRequestId);
        return ResponseUtil.success(result, "Applications retrieved successfully");
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(
            summary = "Update the status of a driver application",
            description = "Assigns an admin-managed status label (e.g. Shortlisted, On Hold) to a driver application. The status ID must reference an active status from the tour application statuses list. Cannot be applied to withdrawn applications."
    )
    public ResponseEntity<Response<TourDriverApplicationSummaryDto>> updateStatus(
            @Parameter(description = "Tour request ID") @PathVariable Long tourRequestId,
            @Parameter(description = "Application ID") @PathVariable Long applicationId,
            @Valid @RequestBody TourApplicationDecisionDto request) {
        log.info("Received request to update status of application ID: {} for tour ID: {}",
                applicationId, tourRequestId);
        TourDriverApplicationSummaryDto result =
                tourAssignmentService.updateApplicationStatus(tourRequestId, applicationId, request);
        log.info("Successfully updated status of application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application status updated successfully");
    }

    @PatchMapping("/{applicationId}/accept")
    @Operation(
            summary = "Accept a driver and create an assignment",
            description = "Formally accepts a driver for the tour. Creates a tour assignment record and transitions the tour status from PUBLISHED to ASSIGNED. Returns 409 if a driver has already been assigned to this tour. Returns 400 if the application is withdrawn or the tour is not in PUBLISHED status."
    )
    public ResponseEntity<Response<TourAssignmentResponseDto>> accept(
            @Parameter(description = "Tour request ID") @PathVariable Long tourRequestId,
            @Parameter(description = "Application ID") @PathVariable Long applicationId) {
        log.info("Received request to accept application ID: {} for tour ID: {}",
                applicationId, tourRequestId);
        TourAssignmentResponseDto result =
                tourAssignmentService.acceptApplication(tourRequestId, applicationId);
        log.info("Successfully accepted application ID: {}, created assignment ID: {}",
                applicationId, result.getId());
        return ResponseUtil.created(result, "Driver accepted and assigned successfully");
    }
}
