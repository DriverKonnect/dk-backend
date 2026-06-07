package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.auth.LoginRequestDto;
import com.driverkonnect.backend.dto.response.auth.AuthResponseDto;
import com.driverkonnect.backend.dto.response.auth.TokenPairDto;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AuthService;
import com.driverkonnect.backend.util.ResponseUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request,
                                                  HttpServletResponse response) {
        log.info("POST /api/auth/login - request received for email: {}", request.getEmail());
        TokenPairDto result = authService.login(request);
        setTokenCookies(response, result.getAccessToken(), result.getRefreshToken());
        log.info("POST /api/auth/login - completed successfully for email: {}", request.getEmail());
        return ResponseEntity.ok(new AuthResponseDto(result.getUser()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(HttpServletRequest request,
                                                    HttpServletResponse response) {
        log.info("POST /api/auth/refresh - token refresh request received");
        String refreshToken = extractRefreshTokenCookie(request);
        TokenPairDto result = authService.refreshAccessToken(refreshToken);
        setTokenCookies(response, result.getAccessToken(), result.getRefreshToken());
        log.info("POST /api/auth/refresh - completed successfully");
        return ResponseEntity.ok(new AuthResponseDto(result.getUser()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<String>> logout(HttpServletRequest request,
                                                    HttpServletResponse response) {
        log.info("POST /api/auth/logout - logout request received");
        String refreshToken = extractRefreshTokenCookie(request);
        authService.logout(refreshToken);
        clearTokenCookies(response);
        log.info("POST /api/auth/logout - completed successfully");
        return ResponseUtil.success(null, "Logged out successfully");
    }

    private void setTokenCookies(HttpServletResponse response,
                                  String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("accessToken", accessToken, jwtExpiration / 1000).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("refreshToken", refreshToken, refreshExpiration / 1000).toString());
    }

    private void clearTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("accessToken", "", 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("refreshToken", "", 0).toString());
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

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        if (cookie == null || cookie.getValue().isBlank()) {
            throw new CustomException("Refresh token cookie is missing", 400);
        }
        return cookie.getValue();
    }
}
