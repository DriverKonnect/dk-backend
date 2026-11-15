package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.auth.JwtService;
import com.driverkonnect.backend.dto.request.auth.LoginRequestDto;
import com.driverkonnect.backend.dto.response.auth.TokenPairDto;
import com.driverkonnect.backend.dto.response.user.UserResponseDto;
import com.driverkonnect.backend.entity.RefreshToken;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.DriverApplicationRepository;
import com.driverkonnect.backend.repository.TourCompanyProfileRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final DriverApplicationRepository driverApplicationRepository;
    private final TourCompanyProfileRepository tourCompanyProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenPairDto login(LoginRequestDto request) {
        request.setEmail(request.getEmail().strip().toLowerCase());
        log.debug("Authenticating user with email: {}", request.getEmail());

        rejectInactiveTourCompany(request);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow();

        log.debug("Authentication successful, generating tokens for user: {}", user.getUsername());
        String accessToken = jwtService.generateToken(user.getUsername(), user.getUserRole().getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        log.debug("Tokens generated successfully for user: {}", user.getUsername());

        return new TokenPairDto(accessToken, refreshToken.getToken(), toUserResponseDto(user));
    }

    @Override
    public TokenPairDto refreshAccessToken(String refreshTokenValue) {
        log.debug("Processing access token refresh");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException("Refresh token not found",
                        HttpStatus.NOT_FOUND.value()));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        log.debug("Refresh token valid, generating new access token for user: {}", user.getUsername());

        String newAccessToken = jwtService.generateToken(user.getUsername(), user.getUserRole().getRole());
        return new TokenPairDto(newAccessToken, refreshToken.getToken(), toUserResponseDto(user));
    }

    @Override
    public void logout(String refreshTokenValue) {
        String username = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getName();
        log.debug("Processing logout for user: {}", username);

        refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException("User is already logged out", 400));

        refreshTokenService.deleteKeyByUsername(username);
        log.debug("Logout completed for user: {}", username);
    }

    /**
     * Gives tour companies that are not yet approved (or are rejected/deactivated) a specific reason
     * instead of the generic 401. Only revealed once the password matches, so it does not leak
     * which emails are registered. Wrong password falls through to the normal 401 flow.
     */
    private void rejectInactiveTourCompany(LoginRequestDto request) {
        userRepository.findByEmail(request.getEmail())
                .filter(user -> "TOUR_COMPANY".equals(user.getUserRole().getRole()))
                .filter(user -> !Boolean.TRUE.equals(user.getIsActive()))
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .flatMap(user -> tourCompanyProfileRepository.findByUser_Email(user.getEmail()))
                .ifPresent(profile -> {
                    log.warn("Login blocked for tour company {} with status {}",
                            request.getEmail(), profile.getStatus());
                    String message = switch (profile.getStatus()) {
                        case PENDING -> "Your registration is pending admin approval";
                        case REJECTED -> profile.getRejectionReason() != null
                                ? "Your registration was rejected: " + profile.getRejectionReason()
                                : "Your registration was rejected";
                        case DEACTIVATED, APPROVED -> "Your account has been deactivated. Please contact support";
                    };
                    throw new CustomException(message, HttpStatus.FORBIDDEN.value());
                });
    }

    private UserResponseDto toUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setUserRole(user.getUserRole().getRole());
        dto.setIsFirstLogin(user.getIsFirstLogin());

        if ("DRIVER".equals(user.getUserRole().getRole())) {
            driverApplicationRepository.findByUser_Email(user.getEmail())
                    .ifPresent(app -> {
                        dto.setApplicationId(app.getId());
                        dto.setApplicationStatus(app.getStatus().name());
                    });
        }

        if ("TOUR_COMPANY".equals(user.getUserRole().getRole())) {
            tourCompanyProfileRepository.findByUser_Email(user.getEmail())
                    .ifPresent(profile -> {
                        dto.setCompanyId(profile.getId());
                        dto.setCompanyName(profile.getCompanyName());
                    });
        }

        return dto;
    }
}
