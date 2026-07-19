package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.TourApplicationStatusRequestDto;
import com.driverkonnect.backend.dto.response.admin.TourApplicationStatusResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourApplicationStatusService;
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
@RequestMapping("/api/admin/tour-application-statuses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Tour Application Statuses", description = "Manage the status labels that tour companies can assign to driver applications (e.g. Shortlisted, On Hold, Rejected). These are displayed to both the tour company and the applying driver.")
public class AdminTourApplicationStatusController {

    private final TourApplicationStatusService tourApplicationStatusService;

    @PostMapping
    @Operation(
            summary = "Create a tour application status",
            description = "Adds a new status label to the system. Label must be unique. Returns 409 if a status with the same label already exists."
    )
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> create(
            @Valid @RequestBody TourApplicationStatusRequestDto request) {
        log.info("Received request to create tour application status: {}", request.getLabel());
        TourApplicationStatusResponseDto result = tourApplicationStatusService.create(request);
        log.info("Successfully created tour application status ID: {}", result.getId());
        return ResponseUtil.created(result, "Tour application status created successfully");
    }

    @GetMapping
    @Operation(
            summary = "List all tour application statuses",
            description = "Returns all statuses ordered alphabetically by label, including both active and inactive ones."
    )
    public ResponseEntity<Response<List<TourApplicationStatusResponseDto>>> getAll() {
        log.info("Received request to retrieve all tour application statuses");
        List<TourApplicationStatusResponseDto> result = tourApplicationStatusService.getAll();
        log.info("Successfully retrieved {} tour application statuses", result.size());
        return ResponseUtil.success(result, "Tour application statuses retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tour application status by ID")
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> getById(
            @Parameter(description = "Status ID") @PathVariable Long id) {
        log.info("Received request to retrieve tour application status ID: {}", id);
        TourApplicationStatusResponseDto result = tourApplicationStatusService.getById(id);
        return ResponseUtil.success(result, "Tour application status retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a tour application status",
            description = "Updates the label and description of an existing status. Returns 409 if the new label conflicts with another existing status."
    )
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> update(
            @Parameter(description = "Status ID") @PathVariable Long id,
            @Valid @RequestBody TourApplicationStatusRequestDto request) {
        log.info("Received request to update tour application status ID: {}", id);
        TourApplicationStatusResponseDto result = tourApplicationStatusService.update(id, request);
        log.info("Successfully updated tour application status ID: {}", id);
        return ResponseUtil.success(result, "Tour application status updated successfully");
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(
            summary = "Enable or disable a tour application status",
            description = "Toggles the active status. Inactive statuses cannot be assigned to driver applications by tour companies."
    )
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> toggleActive(
            @Parameter(description = "Status ID") @PathVariable Long id) {
        log.info("Received request to toggle active status for tour application status ID: {}", id);
        TourApplicationStatusResponseDto result = tourApplicationStatusService.toggleActive(id);
        log.info("Tour application status ID: {} is now isActive={}", id, result.getIsActive());
        return ResponseUtil.success(result, "Tour application status updated");
    }
}
