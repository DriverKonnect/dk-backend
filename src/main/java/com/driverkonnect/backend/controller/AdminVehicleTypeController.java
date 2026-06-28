package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.VehicleTypeRequestDto;
import com.driverkonnect.backend.dto.response.admin.VehicleTypeResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.VehicleTypeService;
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
@RequestMapping("/api/admin/vehicle-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @PostMapping
    public ResponseEntity<Response<VehicleTypeResponseDto>> create(
            @Valid @RequestBody VehicleTypeRequestDto request) {
        log.info("Received request to create vehicle type: {}", request.getName());
        VehicleTypeResponseDto result = vehicleTypeService.create(request);
        log.info("Successfully created vehicle type ID: {}", result.getId());
        return ResponseUtil.created(result, "Vehicle type created successfully");
    }

    @GetMapping
    public ResponseEntity<Response<List<VehicleTypeResponseDto>>> getAll() {
        log.info("Received request to retrieve all vehicle types");
        List<VehicleTypeResponseDto> result = vehicleTypeService.getAll();
        log.info("Successfully retrieved {} vehicle types", result.size());
        return ResponseUtil.success(result, "Vehicle types retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<VehicleTypeResponseDto>> getById(@PathVariable Long id) {
        log.info("Received request to retrieve vehicle type ID: {}", id);
        VehicleTypeResponseDto result = vehicleTypeService.getById(id);
        return ResponseUtil.success(result, "Vehicle type retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<VehicleTypeResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody VehicleTypeRequestDto request) {
        log.info("Received request to update vehicle type ID: {}", id);
        VehicleTypeResponseDto result = vehicleTypeService.update(id, request);
        log.info("Successfully updated vehicle type ID: {}", id);
        return ResponseUtil.success(result, "Vehicle type updated successfully");
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Response<VehicleTypeResponseDto>> toggleActive(@PathVariable Long id) {
        log.info("Received request to toggle active status for vehicle type ID: {}", id);
        VehicleTypeResponseDto result = vehicleTypeService.toggleActive(id);
        log.info("Vehicle type ID: {} is now isActive={}", id, result.getIsActive());
        return ResponseUtil.success(result, "Vehicle type status updated");
    }

}
