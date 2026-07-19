package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.driver.ApplyForTourDto;
import com.driverkonnect.backend.dto.response.driver.TourApplicationResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourApplicationService;
import com.driverkonnect.backend.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/driver/tours")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
public class DriverTourController {

    private final TourApplicationService tourApplicationService;

    @GetMapping
    public ResponseEntity<Response<List<TourRequestSummaryDto>>> getPublishedTours() {
        log.info("Received request to retrieve published tours");
        List<TourRequestSummaryDto> result = tourApplicationService.getPublishedTours();
        log.info("Successfully retrieved {} published tours", result.size());
        return ResponseUtil.success(result, "Published tours retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<TourRequestResponseDto>> getPublishedTourById(@PathVariable Long id) {
        log.info("Received request to retrieve published tour ID: {}", id);
        TourRequestResponseDto result = tourApplicationService.getPublishedTourById(id);
        return ResponseUtil.success(result, "Tour retrieved successfully");
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Response<TourApplicationResponseDto>> apply(
            @PathVariable Long id,
            @RequestBody(required = false) ApplyForTourDto request) {
        log.info("Received request to apply for tour ID: {}", id);
        TourApplicationResponseDto result = tourApplicationService.apply(id,
                request != null ? request : new ApplyForTourDto());
        log.info("Successfully applied for tour ID: {}, application ID: {}", id, result.getId());
        return ResponseUtil.created(result, "Application submitted successfully");
    }

    @GetMapping("/applications")
    public ResponseEntity<Response<List<TourApplicationResponseDto>>> getMyApplications() {
        log.info("Received request to retrieve driver's own tour applications");
        List<TourApplicationResponseDto> result = tourApplicationService.getMyApplications();
        log.info("Successfully retrieved {} tour applications", result.size());
        return ResponseUtil.success(result, "Applications retrieved successfully");
    }

    @PatchMapping("/applications/{applicationId}/withdraw")
    public ResponseEntity<Response<TourApplicationResponseDto>> withdraw(
            @PathVariable Long applicationId) {
        log.info("Received request to withdraw tour application ID: {}", applicationId);
        TourApplicationResponseDto result = tourApplicationService.withdraw(applicationId);
        log.info("Successfully withdrew tour application ID: {}", applicationId);
        return ResponseUtil.success(result, "Application withdrawn successfully");
    }
}
