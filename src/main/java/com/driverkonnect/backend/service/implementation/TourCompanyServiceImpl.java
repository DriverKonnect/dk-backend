package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.tourcompany.TourCompanyRegisterRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourCompanyProfileResponseDto;
import com.driverkonnect.backend.entity.TourCompanyProfile;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.entity.UserRole;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.TourCompanyProfileRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.repository.UserRoleRepository;
import com.driverkonnect.backend.service.TourCompanyService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourCompanyServiceImpl implements TourCompanyService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TourCompanyProfileRepository tourCompanyProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public TourCompanyProfileResponseDto register(TourCompanyRegisterRequestDto request) {
        String email = request.getEmail().strip().toLowerCase();
        log.debug("Starting tour company registration for email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration attempt with already-registered email: {}", email);
            throw new CustomException("An account with this email already exists", 409);
        }

        String brn = request.getBusinessRegistrationNumber().strip().toUpperCase();
        if (tourCompanyProfileRepository.existsByBusinessRegistrationNumber(brn)) {
            log.warn("Registration attempt with already-registered BRN: {}", brn);
            throw new CustomException("A company with this business registration number already exists", 409);
        }

        UserRole tourCompanyRole = userRoleRepository.findByRole("TOUR_COMPANY")
                .orElseThrow(() -> {
                    log.error("TOUR_COMPANY role not found in database — check seed data in V1 migration");
                    return new CustomException("TOUR_COMPANY role not configured", 500);
                });

        User user = new User();
        user.setFirstName(request.getContactPersonFirstName().strip());
        user.setLastName(request.getContactPersonLastName().strip());
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(tourCompanyRole);
        user.setIsActive(true);
        user.setIsFirstLogin(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        log.debug("User account created with ID: {}", user.getId());

        TourCompanyProfile profile = new TourCompanyProfile();
        profile.setUser(user);
        profile.setCompanyName(request.getCompanyName().strip());
        profile.setBusinessRegistrationNumber(brn);
        profile.setContactPersonFirstName(request.getContactPersonFirstName().strip());
        profile.setContactPersonLastName(request.getContactPersonLastName().strip());
        profile.setPhone(request.getPhone().strip());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        profile = tourCompanyProfileRepository.save(profile);
        log.debug("Tour company profile created with ID: {} for user ID: {}", profile.getId(), user.getId());

        return toResponseDto(profile);
    }

    @Override
    public TourCompanyProfileResponseDto getMyProfile() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        log.debug("Fetching tour company profile for authenticated user: {}", email);

        TourCompanyProfile profile = tourCompanyProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new CustomException("No profile found for this account", 404));

        return toResponseDto(profile);
    }

    private TourCompanyProfileResponseDto toResponseDto(TourCompanyProfile profile) {
        TourCompanyProfileResponseDto dto = new TourCompanyProfileResponseDto();
        dto.setId(profile.getId());
        dto.setCompanyName(profile.getCompanyName());
        dto.setBusinessRegistrationNumber(profile.getBusinessRegistrationNumber());
        dto.setContactPersonFirstName(profile.getContactPersonFirstName());
        dto.setContactPersonLastName(profile.getContactPersonLastName());
        dto.setEmail(profile.getUser().getEmail());
        dto.setPhone(profile.getPhone());
        dto.setCreatedAt(profile.getCreatedAt());
        return dto;
    }
}
