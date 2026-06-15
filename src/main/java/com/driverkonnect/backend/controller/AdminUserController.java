package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.CreateAdminUserRequestDto;
import com.driverkonnect.backend.dto.request.admin.UpdateAdminStatusRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminUserResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AdminUserService;
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
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Response<List<AdminUserResponseDto>>> getAllAdminUsers() {
        log.info("GET /api/admin/users - request received");
        List<AdminUserResponseDto> result = adminUserService.getAllAdminUsers();
        log.info("GET /api/admin/users - returned {} admin users", result.size());
        return ResponseUtil.success(result, "Admin users retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<Response<AdminUserResponseDto>> createAdminUser(
            @Valid @RequestBody CreateAdminUserRequestDto request) {
        log.info("POST /api/admin/users - create admin request received for email: {}", request.getEmail());
        AdminUserResponseDto result = adminUserService.createAdminUser(request);
        log.info("POST /api/admin/users - admin user created with ID: {}", result.getId());
        return ResponseUtil.created(result, "Admin user created successfully");
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<Response<AdminUserResponseDto>> updateAdminStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateAdminStatusRequestDto request) {
        log.info("PUT /api/admin/users/{}/status - request received, isActive: {}", userId, request.getIsActive());
        AdminUserResponseDto result = adminUserService.updateAdminStatus(userId, request);
        log.info("PUT /api/admin/users/{}/status - status updated successfully", userId);
        return ResponseUtil.success(result, "Admin user status updated successfully");
    }
}
