package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.tourcompany.TourCompanyRegisterRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourCompanyProfileResponseDto;
import com.driverkonnect.backend.entity.TourCompanyProfile;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.entity.UserRole;
import com.driverkonnect.backend.enums.TourCompanyStatus;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourCompanyServiceImpl implements TourCompanyService {

    private static final String TOUR_COMPANY_ROLE = "TOUR_COMPANY";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TourCompanyProfileRepository tourCompanyProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Submits a tour company registration for admin approval. The account is created inactive
     * (status PENDING) and cannot log in until an admin approves it. A previously REJECTED
     * registration with the same email/BRN is resubmitted in place.
     */
    @Override
    @Transactional
    public TourCompanyProfileResponseDto register(TourCompanyRegisterRequestDto request) {
        String email = request.getEmail().strip().toLowerCase();
        String brn = request.getBusinessRegistrationNumber().strip().toUpperCase();
        log.debug("Processing tour company registration request for email: {}", email);

        TourCompanyProfile rejectedProfile = findRejectedProfileForEmail(email);

        Optional<TourCompanyProfile> brnOwner = tourCompanyProfileRepository.findByBusinessRegistrationNumber(brn);
        if (brnOwner.isPresent()
                && (rejectedProfile == null || !brnOwner.get().getId().equals(rejectedProfile.getId()))) {
            log.warn("Registration attempt with already-registered BRN: {}", brn);
            throw new CustomException("A company with this business registration number already exists", 409);
        }

        TourCompanyProfile profile = rejectedProfile != null
                ? resubmit(rejectedProfile, request, brn)
                : createNew(request, email, brn);

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

    /**
     * Returns the REJECTED profile owned by this email (eligible for resubmission), or null if the
     * email is unused. Throws 409 if the email belongs to any other account.
     */
    private TourCompanyProfile findRejectedProfileForEmail(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            return null;
        }

        User user = existingUser.get();
        if (TOUR_COMPANY_ROLE.equals(user.getUserRole().getRole())) {
            Optional<TourCompanyProfile> profile = tourCompanyProfileRepository.findByUser_Email(email);
            if (profile.isPresent() && profile.get().getStatus() == TourCompanyStatus.REJECTED) {
                return profile.get();
            }
        }

        log.warn("Registration attempt with already-registered email: {}", email);
        throw new CustomException("An account with this email already exists", 409);
    }

    private TourCompanyProfile createNew(TourCompanyRegisterRequestDto request, String email, String brn) {
        UserRole tourCompanyRole = userRoleRepository.findByRole(TOUR_COMPANY_ROLE)
                .orElseThrow(() -> {
                    log.error("TOUR_COMPANY role not found in database - check seed data in V1 migration");
                    return new CustomException("TOUR_COMPANY role not configured", 500);
                });

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setFirstName(request.getContactPersonFirstName().strip());
        user.setLastName(request.getContactPersonLastName().strip());
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(tourCompanyRole);
        user.setIsActive(false);
        user.setIsFirstLogin(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user = userRepository.save(user);
        log.debug("Inactive user account created with ID: {}", user.getId());

        TourCompanyProfile profile = new TourCompanyProfile();
        profile.setUser(user);
        applyRequest(profile, request, brn);
        profile.setStatus(TourCompanyStatus.PENDING);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profile = tourCompanyProfileRepository.save(profile);
        log.debug("Tour company registration ID: {} created with status PENDING", profile.getId());

        return profile;
    }

    private TourCompanyProfile resubmit(TourCompanyProfile profile, TourCompanyRegisterRequestDto request, String brn) {
        LocalDateTime now = LocalDateTime.now();

        User user = profile.getUser();
        user.setFirstName(request.getContactPersonFirstName().strip());
        user.setLastName(request.getContactPersonLastName().strip());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(false);
        user.setUpdatedAt(now);
        userRepository.save(user);

        applyRequest(profile, request, brn);
        profile.setStatus(TourCompanyStatus.PENDING);
        profile.setRejectionReason(null);
        profile.setReviewedAt(null);
        profile.setReviewedBy(null);
        profile.setUpdatedAt(now);
        profile = tourCompanyProfileRepository.save(profile);
        log.debug("Rejected tour company registration ID: {} resubmitted, status reset to PENDING", profile.getId());

        return profile;
    }

    private void applyRequest(TourCompanyProfile profile, TourCompanyRegisterRequestDto request, String brn) {
        profile.setCompanyName(request.getCompanyName().strip());
        profile.setBusinessRegistrationNumber(brn);
        profile.setContactPersonFirstName(request.getContactPersonFirstName().strip());
        profile.setContactPersonLastName(request.getContactPersonLastName().strip());
        profile.setPhone(request.getPhone().strip());
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
        dto.setStatus(profile.getStatus().name());
        dto.setCreatedAt(profile.getCreatedAt());
        return dto;
    }
}
