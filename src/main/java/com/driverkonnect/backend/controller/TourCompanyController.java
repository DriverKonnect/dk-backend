package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.account.PasswordUpdateRequestDto;
import com.driverkonnect.backend.dto.request.tourcompany.TourCompanyRegisterRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourCompanyProfileResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AccountService;
import com.driverkonnect.backend.service.TourCompanyService;
import com.driverkonnect.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tour-company")
@RequiredArgsConstructor
@Tag(name = "Tour Company - Registration & Profile", description = "Tour company registration requests (subject to admin approval), profile view, and password update.")
public class TourCompanyController {

    private final TourCompanyService tourCompanyService;
    private final AccountService accountService;

    @PostMapping("/register")
    @Operation(
            summary = "Submit a tour company registration request",
            description = "Submits a tour company registration for admin review. The account is created in PENDING status and cannot log in until an admin approves it. A previously REJECTED registration can be resubmitted with the same email/BRN. Returns 409 if the email or business registration number is already in use by a pending, approved, or deactivated company."
    )
    public ResponseEntity<Response<TourCompanyProfileResponseDto>> register(
            @Valid @RequestBody TourCompanyRegisterRequestDto request) {
        log.info("Received tour company registration request");
        TourCompanyProfileResponseDto result = tourCompanyService.register(request);
        log.info("Tour company registration ID: {} submitted with status {}", result.getId(), result.getStatus());
        return ResponseUtil.created(result, "Registration submitted. Awaiting admin approval.");
    }

    @GetMapping("/profile/me")
    @Operation(
            summary = "Get my company profile",
            description = "Retrieves the authenticated tour company's own profile."
    )
    public ResponseEntity<Response<TourCompanyProfileResponseDto>> getMyProfile() {
        log.info("Received request to retrieve tour company profile");
        TourCompanyProfileResponseDto result = tourCompanyService.getMyProfile();
        log.info("Successfully retrieved tour company profile ID: {}", result.getId());
        return ResponseUtil.success(result, "Profile retrieved successfully");
    }

    @PutMapping("/password")
    @Operation(
            summary = "Update company password",
            description = "Updates the authenticated tour company's account password."
    )
    public ResponseEntity<Response<String>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequestDto request) {
        log.info("Received request to update tour company password");
        accountService.updatePassword(request);
        log.info("Successfully updated tour company password");
        return ResponseUtil.success(null, "Password updated successfully. Please log in again.");
    }
}
