package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.AdminRejectRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverSummaryDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AdminService;
import com.driverkonnect.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Driver Applications", description = "Review, approve, reject, and deactivate driver registration applications.")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/drivers")
    @Operation(
            summary = "List all driver applications",
            description = "Returns a summary of every driver application in the system, regardless of status, for admin review."
    )
    public ResponseEntity<Response<List<AdminDriverSummaryDto>>> getAllApplications() {
        log.info("Received request to retrieve all driver applications");
        List<AdminDriverSummaryDto> result = adminService.getAllApplications();
        log.info("Successfully retrieved {} driver applications", result.size());
        return ResponseUtil.success(result, "Applications retrieved successfully");
    }

    @GetMapping("/drivers/{applicationId}")
    @Operation(
            summary = "Get a driver application by ID",
            description = "Retrieves the full details of a single driver application. Returns 404 if no application exists with the given ID."
    )
    public ResponseEntity<Response<AdminDriverResponseDto>> getApplication(
            @Parameter(description = "Driver application ID") @PathVariable Long applicationId) {
        log.info("Received request to retrieve driver application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.getApplication(applicationId);
        log.info("Successfully retrieved driver application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application retrieved successfully");
    }

    @PutMapping("/drivers/{applicationId}/approve")
    @Operation(
            summary = "Approve a driver application",
            description = "Approves a pending driver application, activating the driver's account. Returns 404 if no application exists with the given ID."
    )
    public ResponseEntity<Response<AdminDriverResponseDto>> approveApplication(
            @Parameter(description = "Driver application ID") @PathVariable Long applicationId) {
        log.info("Received request to approve driver application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.approveApplication(applicationId);
        log.info("Successfully approved driver application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application approved successfully");
    }

    @PutMapping("/drivers/{applicationId}/reject")
    @Operation(
            summary = "Reject a driver application",
            description = "Rejects a pending driver application. An optional rejection reason can be supplied in the request body. Returns 404 if no application exists with the given ID."
    )
    public ResponseEntity<Response<AdminDriverResponseDto>> rejectApplication(
            @Parameter(description = "Driver application ID") @PathVariable Long applicationId,
            @RequestBody(required = false) AdminRejectRequestDto request) {
        log.info("Received request to reject driver application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.rejectApplication(
                applicationId, request != null ? request : new AdminRejectRequestDto());
        log.info("Successfully rejected driver application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application rejected");
    }

    @PutMapping("/drivers/{applicationId}/deactivate")
    @Operation(
            summary = "Deactivate a driver account",
            description = "Deactivates a previously approved driver's account, revoking their access. Returns 404 if no application exists with the given ID."
    )
    public ResponseEntity<Response<AdminDriverResponseDto>> deactivateDriver(
            @Parameter(description = "Driver application ID") @PathVariable Long applicationId) {
        log.info("Received request to deactivate driver account for application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.deactivateDriver(applicationId);
        log.info("Successfully deactivated driver account for application ID: {}", applicationId);
        return ResponseUtil.success(result, "Driver account deactivated");
    }
}
