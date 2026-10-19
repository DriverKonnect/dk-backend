package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.VehicleRejectionDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverVehicleSummaryDto;
import com.driverkonnect.backend.enums.VehicleApprovalStatus;
import com.driverkonnect.backend.generics.PagedResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.DriverVehicleService;
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

@Slf4j
@RestController
@RequestMapping("/api/admin/driver-vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Driver Vehicles", description = "Review and approve or reject vehicles submitted by drivers. Approved vehicles can be used when applying for tour requests.")
public class AdminDriverVehicleController {

    private final DriverVehicleService driverVehicleService;

    @GetMapping
    @Operation(
            summary = "List all driver vehicles",
            description = "Returns a paginated list of all active driver vehicles. Filter by approval status (PENDING, APPROVED, REJECTED) to focus on vehicles awaiting review."
    )
    public ResponseEntity<Response<PagedResponseDto<AdminDriverVehicleSummaryDto>>> getAllVehicles(
            @Parameter(description = "Filter by approval status") @RequestParam(required = false) VehicleApprovalStatus status,
            @Parameter(description = "Page number (0-indexed, default 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10)") @RequestParam(defaultValue = "10") int size) {
        log.info("Admin requesting driver vehicles — status={}, page={}, size={}", status, page, size);
        PagedResponseDto<AdminDriverVehicleSummaryDto> result = driverVehicleService.getAllVehicles(status, page, size);
        return ResponseUtil.success(result, "Driver vehicles retrieved successfully");
    }

    @PatchMapping("/{id}/approve")
    @Operation(
            summary = "Approve a vehicle",
            description = "Approves a driver's vehicle, making it available for the driver to select when applying for tour requests. Returns 409 if the vehicle is already approved."
    )
    public ResponseEntity<Response<AdminDriverVehicleSummaryDto>> approveVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Admin approving vehicle ID: {}", id);
        AdminDriverVehicleSummaryDto result = driverVehicleService.approveVehicle(id);
        log.info("Vehicle ID: {} approved", id);
        return ResponseUtil.success(result, "Vehicle approved successfully");
    }

    @PatchMapping("/{id}/reject")
    @Operation(
            summary = "Reject a vehicle",
            description = "Rejects a driver's vehicle with a mandatory reason. The driver will see the rejection reason and can update and resubmit the vehicle. Returns 409 if the vehicle is already rejected."
    )
    public ResponseEntity<Response<AdminDriverVehicleSummaryDto>> rejectVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody VehicleRejectionDto request) {
        log.info("Admin rejecting vehicle ID: {}", id);
        AdminDriverVehicleSummaryDto result = driverVehicleService.rejectVehicle(id, request.getRejectionReason());
        log.info("Vehicle ID: {} rejected", id);
        return ResponseUtil.success(result, "Vehicle rejected successfully");
    }
}
