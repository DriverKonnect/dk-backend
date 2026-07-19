package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.tourcompany.TourRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourRequestService;
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
@RequestMapping("/api/tour-company/tours")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TOUR_COMPANY')")
public class TourRequestController {

    private final TourRequestService tourRequestService;

    @PostMapping
    public ResponseEntity<Response<TourRequestResponseDto>> create(
            @Valid @RequestBody TourRequestDto request) {
        log.info("Received request to create tour request: {}", request.getTourName());
        TourRequestResponseDto result = tourRequestService.create(request);
        log.info("Successfully created tour request ID: {}", result.getId());
        return ResponseUtil.created(result, "Tour request created successfully");
    }

    @GetMapping
    public ResponseEntity<Response<List<TourRequestSummaryDto>>> getMyTours() {
        log.info("Received request to retrieve tour company's tour requests");
        List<TourRequestSummaryDto> result = tourRequestService.getMyTours();
        log.info("Successfully retrieved {} tour requests", result.size());
        return ResponseUtil.success(result, "Tour requests retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<TourRequestResponseDto>> getById(@PathVariable Long id) {
        log.info("Received request to retrieve tour request ID: {}", id);
        TourRequestResponseDto result = tourRequestService.getById(id);
        return ResponseUtil.success(result, "Tour request retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<TourRequestResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody TourRequestDto request) {
        log.info("Received request to update tour request ID: {}", id);
        TourRequestResponseDto result = tourRequestService.update(id, request);
        log.info("Successfully updated tour request ID: {}", id);
        return ResponseUtil.success(result, "Tour request updated successfully");
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<Response<TourRequestResponseDto>> publish(@PathVariable Long id) {
        log.info("Received request to publish tour request ID: {}", id);
        TourRequestResponseDto result = tourRequestService.publish(id);
        log.info("Successfully published tour request ID: {}", id);
        return ResponseUtil.success(result, "Tour request published successfully");
    }
}
