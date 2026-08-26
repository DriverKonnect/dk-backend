package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.driver.ApplyForTourDto;
import com.driverkonnect.backend.dto.response.driver.DriverActiveTourDto;
import com.driverkonnect.backend.dto.response.driver.DriverHistorySummaryDto;
import com.driverkonnect.backend.dto.response.driver.TourApplicationResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourApplicationService;
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
@RequestMapping("/api/driver/tours")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "Driver - Tours & Applications", description = "Browse available tours and manage tour applications. Drivers can apply for published tours, track their application statuses, and withdraw if needed.")
public class DriverTourController {

    private final TourApplicationService tourApplicationService;

    @GetMapping
    @Operation(
            summary = "Browse published tours",
            description = "Returns all tours currently in PUBLISHED status, ordered by creation date. These are tours actively seeking drivers."
    )
    public ResponseEntity<Response<List<TourRequestSummaryDto>>> getPublishedTours() {
        log.info("Received request to retrieve published tours");
        List<TourRequestSummaryDto> result = tourApplicationService.getPublishedTours();
        log.info("Successfully retrieved {} published tours", result.size());
        return ResponseUtil.success(result, "Published tours retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a published tour by ID",
            description = "Returns full details of a specific published tour including all pickup and dropoff locations, vehicle requirements, and payment terms. Returns 404 if the tour is not published."
    )
    public ResponseEntity<Response<TourRequestResponseDto>> getPublishedTourById(
            @Parameter(description = "Tour request ID") @PathVariable Long id) {
        log.info("Received request to retrieve published tour ID: {}", id);
        TourRequestResponseDto result = tourApplicationService.getPublishedTourById(id);
        return ResponseUtil.success(result, "Tour retrieved successfully");
    }

    @PostMapping("/{id}/apply")
    @Operation(
            summary = "Apply for a tour",
            description = "Submits an application for the specified published tour. Requires the ID of an approved vehicle from the driver's fleet. The vehicle's per-km rate is snapshotted at application time. An optional note can be included. Returns 400 if the vehicle is not approved or the tour is not published. Returns 409 if the driver has already applied and not withdrawn."
    )
    public ResponseEntity<Response<TourApplicationResponseDto>> apply(
            @Parameter(description = "Tour request ID") @PathVariable Long id,
            @Valid @RequestBody ApplyForTourDto request) {
        log.info("Received request to apply for tour ID: {}", id);
        TourApplicationResponseDto result = tourApplicationService.apply(id, request);
        log.info("Successfully applied for tour ID: {}, application ID: {}", id, result.getId());
        return ResponseUtil.created(result, "Application submitted successfully");
    }

    @GetMapping("/applications")
    @Operation(
            summary = "List own tour applications",
            description = "Returns all tour applications submitted by the authenticated driver, ordered by application date descending. Includes current status label and withdrawal state."
    )
    public ResponseEntity<Response<List<TourApplicationResponseDto>>> getMyApplications() {
        log.info("Received request to retrieve driver's own tour applications");
        List<TourApplicationResponseDto> result = tourApplicationService.getMyApplications();
        log.info("Successfully retrieved {} tour applications", result.size());
        return ResponseUtil.success(result, "Applications retrieved successfully");
    }

    @GetMapping("/history")
    @Operation(
            summary = "Get tour history",
            description = "Returns a paginated list of the driver's completed and cancelled tours, plus lifetime stats: total earned (sum of estimatedKm × per-km rate for completed tours), completed count, and average rating. Ordered by assignment date descending."
    )
    public ResponseEntity<Response<DriverHistorySummaryDto>> getMyHistory(
            @Parameter(description = "Page number (0-indexed, default 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10)") @RequestParam(defaultValue = "10") int size) {
        log.info("Driver requesting tour history — page={}, size={}", page, size);
        DriverHistorySummaryDto result = tourApplicationService.getMyHistory(page, size);
        return ResponseUtil.success(result, "Tour history retrieved successfully");
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active tour",
            description = "Returns the driver's currently active tour — one in ASSIGNED or IN_PROGRESS status. Includes full location list, estimated earnings (estimatedKm × per-km rate), and payment details. Returns 404 if the driver has no active tour."
    )
    public ResponseEntity<Response<DriverActiveTourDto>> getMyActiveTour() {
        log.info("Driver requesting active tour");
        DriverActiveTourDto result = tourApplicationService.getMyActiveTour();
        return ResponseUtil.success(result, "Active tour retrieved successfully");
    }

    @PatchMapping("/{id}/start")
    @Operation(
            summary = "Mark tour as started",
            description = "Transitions the assigned tour to IN_PROGRESS status, indicating the driver has begun the tour. Only works when the tour is in ASSIGNED status — returns 400 if already started or not assigned to this driver."
    )
    public ResponseEntity<Response<DriverActiveTourDto>> startTour(
            @Parameter(description = "Tour request ID") @PathVariable Long id) {
        log.info("Driver marking tour ID: {} as started", id);
        DriverActiveTourDto result = tourApplicationService.startTour(id);
        log.info("Tour ID: {} marked as IN_PROGRESS", id);
        return ResponseUtil.success(result, "Tour started successfully");
    }

    @PatchMapping("/applications/{applicationId}/withdraw")
    @Operation(
            summary = "Withdraw a tour application",
            description = "Marks the specified application as withdrawn. This action cannot be undone - a driver must re-apply if they change their mind. Returns 400 if the application is already withdrawn."
    )
    public ResponseEntity<Response<TourApplicationResponseDto>> withdraw(
            @Parameter(description = "Application ID") @PathVariable Long applicationId) {
        log.info("Received request to withdraw tour application ID: {}", applicationId);
        TourApplicationResponseDto result = tourApplicationService.withdraw(applicationId);
        log.info("Successfully withdrew tour application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application withdrawn successfully");
    }
}
