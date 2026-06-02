package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.driver.DriverRegisterRequestDto;
import com.driverkonnect.backend.dto.response.driver.DriverApplicationResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.DriverRegistrationService;
import com.driverkonnect.backend.util.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/driver")
public class DriverRegistrationController {

    private final DriverRegistrationService driverRegistrationService;

    public DriverRegistrationController(DriverRegistrationService driverRegistrationService) {
        this.driverRegistrationService = driverRegistrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Response<DriverApplicationResponseDto>> register(
            @Valid @RequestBody DriverRegisterRequestDto request) {
        DriverApplicationResponseDto result = driverRegistrationService.register(request);
        return ResponseUtil.created(result, "Registration submitted successfully");
    }

    @PostMapping(value = "/register/{applicationId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<DriverApplicationResponseDto>> uploadDocuments(
            @PathVariable Long applicationId,
            @RequestParam("licenceFront") MultipartFile licenceFront,
            @RequestParam("licenceBack") MultipartFile licenceBack,
            @RequestParam("policeClearance") MultipartFile policeClearance) {
        DriverApplicationResponseDto result = driverRegistrationService.uploadDocuments(
                applicationId, licenceFront, licenceBack, policeClearance);
        return ResponseUtil.success(result, "Documents uploaded successfully. Application is now under review.");
    }

    @GetMapping("/application/me")
    public ResponseEntity<Response<DriverApplicationResponseDto>> getMyApplication() {
        DriverApplicationResponseDto result = driverRegistrationService.getMyApplication();
        return ResponseUtil.success(result, "Application retrieved successfully");
    }
}
