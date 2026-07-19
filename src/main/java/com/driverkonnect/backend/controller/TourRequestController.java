package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.tourcompany.TourRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.enums.TourStatus;
import com.driverkonnect.backend.generics.PagedResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourRequestService;
import com.driverkonnect.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/tour-company/tours")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TOUR_COMPANY')")
@Tag(name = "Tour Company - Tour Requests", description = "Create and manage tour requests. A tour is saved as a draft first, then published to become visible to drivers.")
public class TourRequestController {

    private final TourRequestService tourRequestService;

    @PostMapping
    @Operation(
            summary = "Create a tour request",
            description = "Creates a new tour request in DRAFT status. Must include at least one pickup and one dropoff location. The selected vehicle type must exist and be active."
    )
    public ResponseEntity<Response<TourRequestResponseDto>> create(
            @Valid @RequestBody TourRequestDto request) {
        log.info("Received request to create tour request: {}", request.getTourName());
        TourRequestResponseDto result = tourRequestService.create(request);
        log.info("Successfully created tour request ID: {}", result.getId());
        return ResponseUtil.created(result, "Tour request created successfully");
    }

    @GetMapping
    @Operation(
            summary = "List own tour requests",
            description = "Returns a paginated list of tour requests belonging to the authenticated tour company. " +
                    "Supports optional filtering by status (DRAFT, PUBLISHED, ASSIGNED, COMPLETED, CANCELLED) " +
                    "and by date range (dateFrom/dateTo filter on the tour's start date). " +
                    "Results include driver name, star rating (1–5), payment amount, and payment status where available. " +
                    "Ordered by creation date descending."
    )
    public ResponseEntity<Response<PagedResponseDto<TourRequestSummaryDto>>> getMyTours(
            @Parameter(description = "Filter by tour status") @RequestParam(required = false) TourStatus status,
            @Parameter(description = "Filter tours with start date on or after this date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Filter tours with start date on or before this date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Page number (0-indexed, default 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10)") @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to retrieve tour company's tour requests — status={}, dateFrom={}, dateTo={}, page={}, size={}",
                status, dateFrom, dateTo, page, size);
        PagedResponseDto<TourRequestSummaryDto> result = tourRequestService.getMyTours(status, dateFrom, dateTo, page, size);
        log.info("Successfully retrieved {} tour requests (page {}/{})", result.getContent().size(), page, result.getTotalPages());
        return ResponseUtil.success(result, "Tour requests retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a tour request by ID",
            description = "Returns the full detail of a tour request including all locations. Returns 404 if the tour does not belong to the authenticated tour company."
    )
    public ResponseEntity<Response<TourRequestResponseDto>> getById(
            @Parameter(description = "Tour request ID") @PathVariable Long id) {
        log.info("Received request to retrieve tour request ID: {}", id);
        TourRequestResponseDto result = tourRequestService.getById(id);
        return ResponseUtil.success(result, "Tour request retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a tour request",
            description = "Updates all fields and locations of an existing tour request. Only allowed when the tour is in DRAFT status - published or assigned tours cannot be edited."
    )
    public ResponseEntity<Response<TourRequestResponseDto>> update(
            @Parameter(description = "Tour request ID") @PathVariable Long id,
            @Valid @RequestBody TourRequestDto request) {
        log.info("Received request to update tour request ID: {}", id);
        TourRequestResponseDto result = tourRequestService.update(id, request);
        log.info("Successfully updated tour request ID: {}", id);
        return ResponseUtil.success(result, "Tour request updated successfully");
    }

    @PatchMapping("/{id}/publish")
    @Operation(
            summary = "Publish a tour request",
            description = "Transitions a tour request from DRAFT to PUBLISHED, making it visible to drivers who can then apply. Can only be called once - published tours cannot be reverted to draft."
    )
    public ResponseEntity<Response<TourRequestResponseDto>> publish(
            @Parameter(description = "Tour request ID") @PathVariable Long id) {
        log.info("Received request to publish tour request ID: {}", id);
        TourRequestResponseDto result = tourRequestService.publish(id);
        log.info("Successfully published tour request ID: {}", id);
        return ResponseUtil.success(result, "Tour request published successfully");
    }
}
