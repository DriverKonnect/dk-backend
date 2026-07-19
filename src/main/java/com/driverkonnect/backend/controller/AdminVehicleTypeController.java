package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.VehicleTypeRequestDto;
import com.driverkonnect.backend.dto.response.admin.VehicleTypeResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.VehicleTypeService;
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
@RequestMapping("/api/admin/vehicle-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Vehicle Types", description = "Manage the list of vehicle types available for tour requests. Changes here affect what tour companies can select when creating a tour.")
public class AdminVehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @PostMapping
    @Operation(
            summary = "Create a vehicle type",
            description = "Adds a new vehicle type to the system. If a vehicle type with the same name exists but is inactive, it will be reactivated instead of creating a duplicate."
    )
    public ResponseEntity<Response<VehicleTypeResponseDto>> create(
            @Valid @RequestBody VehicleTypeRequestDto request) {
        log.info("Received request to create vehicle type: {}", request.getName());
        VehicleTypeResponseDto result = vehicleTypeService.create(request);
        log.info("Successfully created vehicle type ID: {}", result.getId());
        return ResponseUtil.created(result, "Vehicle type created successfully");
    }

    @GetMapping
    @Operation(
            summary = "List all vehicle types",
            description = "Returns all vehicle types ordered alphabetically by name, including both active and inactive ones."
    )
    public ResponseEntity<Response<List<VehicleTypeResponseDto>>> getAll() {
        log.info("Received request to retrieve all vehicle types");
        List<VehicleTypeResponseDto> result = vehicleTypeService.getAll();
        log.info("Successfully retrieved {} vehicle types", result.size());
        return ResponseUtil.success(result, "Vehicle types retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a vehicle type by ID")
    public ResponseEntity<Response<VehicleTypeResponseDto>> getById(
            @Parameter(description = "Vehicle type ID") @PathVariable Long id) {
        log.info("Received request to retrieve vehicle type ID: {}", id);
        VehicleTypeResponseDto result = vehicleTypeService.getById(id);
        return ResponseUtil.success(result, "Vehicle type retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a vehicle type",
            description = "Updates the name and description of an existing vehicle type. Returns 409 if the new name conflicts with another existing vehicle type."
    )
    public ResponseEntity<Response<VehicleTypeResponseDto>> update(
            @Parameter(description = "Vehicle type ID") @PathVariable Long id,
            @Valid @RequestBody VehicleTypeRequestDto request) {
        log.info("Received request to update vehicle type ID: {}", id);
        VehicleTypeResponseDto result = vehicleTypeService.update(id, request);
        log.info("Successfully updated vehicle type ID: {}", id);
        return ResponseUtil.success(result, "Vehicle type updated successfully");
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(
            summary = "Enable or disable a vehicle type",
            description = "Toggles the active status of a vehicle type. Inactive vehicle types are hidden from tour companies during tour creation."
    )
    public ResponseEntity<Response<VehicleTypeResponseDto>> toggleActive(
            @Parameter(description = "Vehicle type ID") @PathVariable Long id) {
        log.info("Received request to toggle active status for vehicle type ID: {}", id);
        VehicleTypeResponseDto result = vehicleTypeService.toggleActive(id);
        log.info("Vehicle type ID: {} is now isActive={}", id, result.getIsActive());
        return ResponseUtil.success(result, "Vehicle type status updated");
    }
}
