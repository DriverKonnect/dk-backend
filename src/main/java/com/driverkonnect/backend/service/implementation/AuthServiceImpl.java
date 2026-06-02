package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.auth.JwtService;
import com.driverkonnect.backend.dto.request.auth.LoginRequestDto;
import com.driverkonnect.backend.dto.response.auth.TokenPairDto;
import com.driverkonnect.backend.dto.response.user.UserResponseDto;
import com.driverkonnect.backend.entity.RefreshToken;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.DriverApplicationRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final DriverApplicationRepository driverApplicationRepository;


    @Override
    public TokenPairDto login(LoginRequestDto request) {
        request.setEmail(request.getEmail().strip().toLowerCase());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow();

        String accessToken = jwtService.generateToken(
                user.getUsername(),
                user.getUserRole().getRole());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return new TokenPairDto(accessToken, refreshToken.getToken(), toUserResponseDto(user));
    }

    @Override
    public TokenPairDto refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException("Refresh token not found",
                        HttpStatus.NOT_FOUND.value()));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(
                user.getUsername(), user.getUserRole().getRole());

        return new TokenPairDto(newAccessToken, refreshToken.getToken(), toUserResponseDto(user));
    }

    @Override
    public void logout(String refreshTokenValue) {
        String username = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getName();

        refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException("User is already logged out", 400));

        refreshTokenService.deleteKeyByUsername(username);
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
                    .ifPresent(app -> dto.setApplicationStatus(app.getStatus().name()));
        }

        return dto;
    }
}
