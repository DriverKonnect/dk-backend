package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.admin.CreateAdminUserRequestDto;
import com.driverkonnect.backend.dto.request.admin.UpdateAdminStatusRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminUserResponseDto;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.entity.UserRole;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.repository.UserRoleRepository;
import com.driverkonnect.backend.service.AdminUserService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    public List<AdminUserResponseDto> getAllAdminUsers() {
        log.debug("Fetching all admin users");
        return userRepository.findAllByUserRole_RoleOrderByCreatedAtDesc("ADMIN")
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserResponseDto createAdminUser(CreateAdminUserRequestDto request) {
        String email = request.getEmail().strip().toLowerCase();
        log.debug("Creating admin user for email: {}", email);

        java.util.Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!"ADMIN".equals(user.getUserRole().getRole())) {
                throw new CustomException("This email is already in use by a non-admin account", 400);
            }
            if (user.getIsActive()) {
                throw new CustomException("An active admin account with this email already exists", 409);
            }
            user.setIsActive(true);
            user.setUpdatedAt(LocalDateTime.now());
            User reactivated = userRepository.save(user);
            log.debug("Existing admin account reactivated for email: {}", email);
            return toResponseDto(reactivated);
        }

        UserRole adminRole = userRoleRepository.findByRole("ADMIN")
                .orElseThrow(() -> {
                    log.error("ADMIN role not found in database");
                    return new CustomException("ADMIN role not configured", 500);
                });

        User user = new User();
        user.setFirstName(request.getFirstName().strip());
        user.setLastName(request.getLastName().strip());
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(adminRole);
        user.setIsActive(true);
        user.setIsFirstLogin(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        log.debug("Admin user created with ID: {}", saved.getId());
        return toResponseDto(saved);
    }

    @Override
    @Transactional
    public AdminUserResponseDto updateAdminStatus(Long userId, UpdateAdminStatusRequestDto request) {
        log.debug("Updating active status for admin user ID: {} to: {}", userId, request.getIsActive());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", 404));

        if (!"ADMIN".equals(user.getUserRole().getRole())) {
            throw new CustomException("User is not an admin", 400);
        }

        String currentUserEmail = AuthUtil.getAuthenticatedUser().getUsername();
        if (user.getEmail().equals(currentUserEmail)) {
            throw new CustomException("You cannot change your own active status", 400);
        }

        if (!request.getIsActive() && userRepository.countByUserRole_Role("ADMIN") <= 1) {
            throw new CustomException("Cannot deactivate the last admin account", 400);
        }

        user.setIsActive(request.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        if (!request.getIsActive()) {
            refreshTokenService.deleteKeyByUsername(user.getUsername());
            log.debug("Admin user ID: {} deactivated and session invalidated", userId);
        } else {
            log.debug("Admin user ID: {} reactivated", userId);
        }

        return toResponseDto(saved);
    }

    private AdminUserResponseDto toResponseDto(User user) {
        AdminUserResponseDto dto = new AdminUserResponseDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());
        dto.setIsFirstLogin(user.getIsFirstLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
