package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.response.driver.DriverDashboardDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.TourApplicationService;
import com.driverkonnect.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "Driver - Dashboard", description = "Driver home screen data — stats summary and new tour request previews.")
public class DriverController {

    private final TourApplicationService tourApplicationService;

    @GetMapping("/dashboard")
    @Operation(
            summary = "Get dashboard summary",
            description = "Returns the driver's home screen data: name, verified status, average rating, this-week tour count, acceptance rate, and up to 5 new published tour requests. On-time rate is not yet computed and is returned as null."
    )
    public ResponseEntity<Response<DriverDashboardDto>> getDashboard() {
        log.info("Driver requesting dashboard");
        DriverDashboardDto result = tourApplicationService.getDashboard();
        return ResponseUtil.success(result, "Dashboard retrieved successfully");
    }
}
