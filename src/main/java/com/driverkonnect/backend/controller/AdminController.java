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
        log.info("GET /api/admin/drivers - request received");
        List<AdminDriverSummaryDto> result = adminService.getAllApplications();
        log.info("GET /api/admin/drivers - returned {} applications", result.size());
        return ResponseUtil.success(result, "Applications retrieved successfully");
    }

    @GetMapping("/drivers/{applicationId}")
    public ResponseEntity<Response<AdminDriverResponseDto>> getApplication(
            @PathVariable Long applicationId) {
        log.info("GET /api/admin/drivers/{} - request received", applicationId);
        AdminDriverResponseDto result = adminService.getApplication(applicationId);
        log.info("GET /api/admin/drivers/{} - completed successfully", applicationId);
        return ResponseUtil.success(result, "Application retrieved successfully");
    }

    @PutMapping("/drivers/{applicationId}/approve")
    public ResponseEntity<Response<AdminDriverResponseDto>> approveApplication(
            @PathVariable Long applicationId) {
        log.info("PUT /api/admin/drivers/{}/approve - request received", applicationId);
        AdminDriverResponseDto result = adminService.approveApplication(applicationId);
        log.info("PUT /api/admin/drivers/{}/approve - application approved", applicationId);
        return ResponseUtil.success(result, "Application approved successfully");
    }

    @PutMapping("/drivers/{applicationId}/reject")
    public ResponseEntity<Response<AdminDriverResponseDto>> rejectApplication(
            @PathVariable Long applicationId,
            @RequestBody(required = false) AdminRejectRequestDto request) {
        log.info("PUT /api/admin/drivers/{}/reject - request received", applicationId);
        AdminDriverResponseDto result = adminService.rejectApplication(
                applicationId, request != null ? request : new AdminRejectRequestDto());
        log.info("PUT /api/admin/drivers/{}/reject - application rejected", applicationId);
        return ResponseUtil.success(result, "Application rejected");
    }

    @PutMapping("/drivers/{applicationId}/deactivate")
    public ResponseEntity<Response<AdminDriverResponseDto>> deactivateDriver(
            @PathVariable Long applicationId) {
        log.info("PUT /api/admin/drivers/{}/deactivate - request received", applicationId);
        AdminDriverResponseDto result = adminService.deactivateDriver(applicationId);
        log.info("PUT /api/admin/drivers/{}/deactivate - driver deactivated", applicationId);
        return ResponseUtil.success(result, "Driver account deactivated");
    }
}
