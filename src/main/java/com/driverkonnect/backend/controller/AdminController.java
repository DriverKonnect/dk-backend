package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.AdminRejectRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverSummaryDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AdminService;
import com.driverkonnect.backend.util.ResponseUtil;
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
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/drivers")
    public ResponseEntity<Response<List<AdminDriverSummaryDto>>> getAllApplications() {
        log.info("Received request to retrieve all driver applications");
        List<AdminDriverSummaryDto> result = adminService.getAllApplications();
        log.info("Successfully retrieved {} driver applications", result.size());
        return ResponseUtil.success(result, "Applications retrieved successfully");
    }

    @GetMapping("/drivers/{applicationId}")
    public ResponseEntity<Response<AdminDriverResponseDto>> getApplication(
            @PathVariable Long applicationId) {
        log.info("Received request to retrieve driver application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.getApplication(applicationId);
        log.info("Successfully retrieved driver application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application retrieved successfully");
    }

    @PutMapping("/drivers/{applicationId}/approve")
    public ResponseEntity<Response<AdminDriverResponseDto>> approveApplication(
            @PathVariable Long applicationId) {
        log.info("Received request to approve driver application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.approveApplication(applicationId);
        log.info("Successfully approved driver application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application approved successfully");
    }

    @PutMapping("/drivers/{applicationId}/reject")
    public ResponseEntity<Response<AdminDriverResponseDto>> rejectApplication(
            @PathVariable Long applicationId,
            @RequestBody(required = false) AdminRejectRequestDto request) {
        log.info("Received request to reject driver application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.rejectApplication(
                applicationId, request != null ? request : new AdminRejectRequestDto());
        log.info("Successfully rejected driver application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application rejected");
    }

    @PutMapping("/drivers/{applicationId}/deactivate")
    public ResponseEntity<Response<AdminDriverResponseDto>> deactivateDriver(
            @PathVariable Long applicationId) {
        log.info("Received request to deactivate driver account for application ID: {}", applicationId);
        AdminDriverResponseDto result = adminService.deactivateDriver(applicationId);
        log.info("Successfully deactivated driver account for application ID: {}", applicationId);
        return ResponseUtil.success(result, "Driver account deactivated");
    }
}
