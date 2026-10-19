package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.driver.DriverVehicleRequestDto;
import com.driverkonnect.backend.dto.response.driver.DriverVehicleResponseDto;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/driver/vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "Driver - Vehicles", description = "Manage the driver's fleet. New vehicles require admin approval before they can be used when applying for tours.")
public class DriverVehicleController {

    private final DriverVehicleService driverVehicleService;

    @PostMapping
    @Operation(
            summary = "Add a vehicle",
            description = "Adds a new vehicle to the driver's fleet. The vehicle is submitted in PENDING status and must be approved by an admin before it can be selected when applying for tours."
    )
    public ResponseEntity<Response<DriverVehicleResponseDto>> addVehicle(
            @Valid @RequestBody DriverVehicleRequestDto request) {
        log.info("Driver requesting to add vehicle: {} {}", request.getBrand(), request.getModel());
        DriverVehicleResponseDto result = driverVehicleService.addVehicle(request);
        log.info("Vehicle added with ID: {}", result.getId());
        return ResponseUtil.created(result, "Vehicle submitted for approval");
    }

    @GetMapping
    @Operation(
            summary = "List own vehicles",
            description = "Returns all active vehicles belonging to the authenticated driver, including their approval status and per-km rate."
    )
    public ResponseEntity<Response<List<DriverVehicleResponseDto>>> getMyVehicles() {
        log.info("Driver requesting vehicle list");
        List<DriverVehicleResponseDto> result = driverVehicleService.getMyVehicles();
        return ResponseUtil.success(result, "Vehicles retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a vehicle by ID",
            description = "Returns the detail of a specific vehicle. Returns 404 if the vehicle does not belong to the authenticated driver."
    )
    public ResponseEntity<Response<DriverVehicleResponseDto>> getVehicleById(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Driver requesting vehicle ID: {}", id);
        DriverVehicleResponseDto result = driverVehicleService.getMyVehicleById(id);
        return ResponseUtil.success(result, "Vehicle retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a vehicle",
            description = "Updates a vehicle's details. If the vehicle was previously approved or rejected, it is reset to PENDING and must be re-approved by an admin."
    )
    public ResponseEntity<Response<DriverVehicleResponseDto>> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody DriverVehicleRequestDto request) {
        log.info("Driver requesting update for vehicle ID: {}", id);
        DriverVehicleResponseDto result = driverVehicleService.updateVehicle(id, request);
        log.info("Vehicle ID: {} updated", id);
        return ResponseUtil.success(result, "Vehicle updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remove a vehicle",
            description = "Soft-deletes a vehicle from the driver's fleet. The vehicle will no longer appear in listings or be selectable when applying for tours."
    )
    public ResponseEntity<Response<Void>> removeVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Driver requesting removal of vehicle ID: {}", id);
        driverVehicleService.removeVehicle(id);
        return ResponseUtil.success(null, "Vehicle removed successfully");
    }
}
