package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.account.PasswordUpdateRequestDto;
import com.driverkonnect.backend.dto.request.auth.LoginRequestDto;
import com.driverkonnect.backend.dto.request.tourcompany.TourCompanyRegisterRequestDto;
import com.driverkonnect.backend.dto.response.auth.TokenPairDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourCompanyProfileResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AccountService;
import com.driverkonnect.backend.service.AuthService;
import com.driverkonnect.backend.service.TourCompanyService;
import com.driverkonnect.backend.util.ResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/tour-company")
public class TourCompanyController {

    private final TourCompanyService tourCompanyService;
    private final AuthService authService;
    private final AccountService accountService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    public TourCompanyController(TourCompanyService tourCompanyService,
                                 AuthService authService,
                                 AccountService accountService) {
        this.tourCompanyService = tourCompanyService;
        this.authService = authService;
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<Response<TourCompanyProfileResponseDto>> register(
            @Valid @RequestBody TourCompanyRegisterRequestDto request,
            HttpServletResponse response) {
        log.info("Received request to register a new tour company");
        TourCompanyProfileResponseDto result = tourCompanyService.register(request);
        TokenPairDto tokens = authService.login(
                new LoginRequestDto(request.getEmail(), request.getPassword()));
        setTokenCookies(response, tokens.getAccessToken(), tokens.getRefreshToken());
        log.info("Successfully registered tour company with profile ID: {}", result.getId());
        return ResponseUtil.created(result, "Tour company registered successfully");
    }

    @GetMapping("/profile/me")
    public ResponseEntity<Response<TourCompanyProfileResponseDto>> getMyProfile() {
        log.info("Received request to retrieve tour company profile");
        TourCompanyProfileResponseDto result = tourCompanyService.getMyProfile();
        log.info("Successfully retrieved tour company profile ID: {}", result.getId());
        return ResponseUtil.success(result, "Profile retrieved successfully");
    }

    @PutMapping("/password")
    public ResponseEntity<Response<String>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequestDto request) {
        log.info("Received request to update tour company password");
        accountService.updatePassword(request);
        log.info("Successfully updated tour company password");
        return ResponseUtil.success(null, "Password updated successfully. Please log in again.");
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
