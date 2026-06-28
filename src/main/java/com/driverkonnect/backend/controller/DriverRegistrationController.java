package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.account.PasswordUpdateRequestDto;
import com.driverkonnect.backend.dto.request.auth.LoginRequestDto;
import com.driverkonnect.backend.dto.request.driver.DriverRegisterRequestDto;
import com.driverkonnect.backend.dto.response.auth.TokenPairDto;
import com.driverkonnect.backend.dto.response.driver.DriverApplicationResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AccountService;
import com.driverkonnect.backend.service.AuthService;
import com.driverkonnect.backend.service.DriverRegistrationService;
import com.driverkonnect.backend.util.ResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/driver")
public class DriverRegistrationController {

    private final DriverRegistrationService driverRegistrationService;
    private final AuthService authService;
    private final AccountService accountService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    public DriverRegistrationController(DriverRegistrationService driverRegistrationService,
                                        AuthService authService,
                                        AccountService accountService) {
        this.driverRegistrationService = driverRegistrationService;
        this.authService = authService;
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<Response<DriverApplicationResponseDto>> register(
            @Valid @RequestBody DriverRegisterRequestDto request,
            HttpServletResponse response) {
        log.info("Received request to register a new driver");
        DriverApplicationResponseDto result = driverRegistrationService.register(request);
        TokenPairDto tokens = authService.login(
                new LoginRequestDto(request.getEmail(), request.getPassword()));
        setTokenCookies(response, tokens.getAccessToken(), tokens.getRefreshToken());
        log.info("Successfully registered driver with application ID: {}", result.getId());
        return ResponseUtil.created(result, "Registration submitted successfully");
    }

    @PostMapping(value = "/register/{applicationId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<DriverApplicationResponseDto>> uploadDocuments(
            @PathVariable Long applicationId,
            @RequestParam("licenceFront") MultipartFile licenceFront,
            @RequestParam("licenceBack") MultipartFile licenceBack,
            @RequestParam("policeClearance") MultipartFile policeClearance) {
        log.info("Received request to upload documents for application ID: {}", applicationId);
        DriverApplicationResponseDto result = driverRegistrationService.uploadDocuments(
                applicationId, licenceFront, licenceBack, policeClearance);
        log.info("Successfully uploaded documents for application ID: {}", applicationId);
        return ResponseUtil.success(result, "Documents uploaded successfully. Application is now under review.");
    }

    @PutMapping("/password")
    public ResponseEntity<Response<String>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequestDto request) {
        log.info("Received request to update driver password");
        accountService.updatePassword(request);
        log.info("Successfully updated driver password");
        return ResponseUtil.success(null, "Password updated successfully. Please log in again.");
    }

    @GetMapping("/application/me")
    public ResponseEntity<Response<DriverApplicationResponseDto>> getMyApplication() {
        log.info("Received request to retrieve driver's own application");
        DriverApplicationResponseDto result = driverRegistrationService.getMyApplication();
        log.info("Successfully retrieved application ID: {}", result.getId());
        return ResponseUtil.success(result, "Application retrieved successfully");
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("accessToken", accessToken, jwtExpiration / 1000).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("refreshToken", refreshToken, refreshExpiration / 1000).toString());
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
