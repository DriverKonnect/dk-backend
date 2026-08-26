package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.driver.UpdateDriverProfileDto;
import com.driverkonnect.backend.dto.response.driver.DriverDashboardDto;
import com.driverkonnect.backend.dto.response.driver.DriverProfileDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.DriverProfileService;
import com.driverkonnect.backend.service.TourApplicationService;
import com.driverkonnect.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "Driver - Dashboard & Profile", description = "Driver home screen data, profile view, and profile editing.")
public class DriverController {

    private final TourApplicationService tourApplicationService;
    private final DriverProfileService driverProfileService;

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

    @GetMapping("/profile")
    @Operation(
            summary = "Get driver profile",
            description = "Returns the authenticated driver's full profile including personal details, languages, years of experience, lifetime stats (tours, rating), and uploaded documents."
    )
    public ResponseEntity<Response<DriverProfileDto>> getProfile() {
        log.info("Driver requesting profile");
        DriverProfileDto result = driverProfileService.getProfile();
        return ResponseUtil.success(result, "Profile retrieved successfully");
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update driver profile",
            description = "Updates the driver's personal details: full name, date of birth, phone, WhatsApp, NIC number, languages spoken, and years of experience. Returns 409 if the new NIC number is already registered to another driver."
    )
    public ResponseEntity<Response<DriverProfileDto>> updateProfile(
            @Valid @RequestBody UpdateDriverProfileDto request) {
        log.info("Driver requesting profile update");
        DriverProfileDto result = driverProfileService.updateProfile(request);
        log.info("Driver profile updated successfully");
        return ResponseUtil.success(result, "Profile updated successfully");
    }
}
