package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.TourApplicationStatusRequestDto;
import com.driverkonnect.backend.dto.response.admin.TourApplicationStatusResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourApplicationStatusService;
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
@RequestMapping("/api/admin/tour-application-statuses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTourApplicationStatusController {

    private final TourApplicationStatusService tourApplicationStatusService;

    @PostMapping
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> create(
            @Valid @RequestBody TourApplicationStatusRequestDto request) {
        log.info("Received request to create tour application status: {}", request.getCode());
        TourApplicationStatusResponseDto result = tourApplicationStatusService.create(request);
        log.info("Successfully created tour application status ID: {}", result.getId());
        return ResponseUtil.created(result, "Tour application status created successfully");
    }

    @GetMapping
    public ResponseEntity<Response<List<TourApplicationStatusResponseDto>>> getAll() {
        log.info("Received request to retrieve all tour application statuses");
        List<TourApplicationStatusResponseDto> result = tourApplicationStatusService.getAll();
        log.info("Successfully retrieved {} tour application statuses", result.size());
        return ResponseUtil.success(result, "Tour application statuses retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> getById(@PathVariable Long id) {
        log.info("Received request to retrieve tour application status ID: {}", id);
        TourApplicationStatusResponseDto result = tourApplicationStatusService.getById(id);
        return ResponseUtil.success(result, "Tour application status retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody TourApplicationStatusRequestDto request) {
        log.info("Received request to update tour application status ID: {}", id);
        TourApplicationStatusResponseDto result = tourApplicationStatusService.update(id, request);
        log.info("Successfully updated tour application status ID: {}", id);
        return ResponseUtil.success(result, "Tour application status updated successfully");
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Response<TourApplicationStatusResponseDto>> toggleActive(@PathVariable Long id) {
        log.info("Received request to toggle active status for tour application status ID: {}", id);
        TourApplicationStatusResponseDto result = tourApplicationStatusService.toggleActive(id);
        log.info("Tour application status ID: {} is now isActive={}", id, result.getIsActive());
        return ResponseUtil.success(result, "Tour application status updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long id) {
        log.info("Received request to delete tour application status ID: {}", id);
        tourApplicationStatusService.delete(id);
        log.info("Successfully deleted tour application status ID: {}", id);
        return ResponseUtil.success(null, "Tour application status deleted successfully");
    }
}
