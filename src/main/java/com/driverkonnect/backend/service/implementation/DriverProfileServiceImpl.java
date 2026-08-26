package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.driver.UpdateDriverProfileDto;
import com.driverkonnect.backend.dto.response.driver.DriverProfileDocumentDto;
import com.driverkonnect.backend.dto.response.driver.DriverProfileDto;
import com.driverkonnect.backend.entity.DriverApplication;
import com.driverkonnect.backend.entity.DriverDocument;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.DriverApplicationRepository;
import com.driverkonnect.backend.repository.TourAssignmentRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.DriverProfileService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverProfileServiceImpl implements DriverProfileService {

    private final DriverApplicationRepository driverApplicationRepository;
    private final UserRepository userRepository;
    private final TourAssignmentRepository tourAssignmentRepository;

    @Override
    @Transactional
    public DriverProfileDto getProfile() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        User user = resolveUser(email);
        DriverApplication application = resolveApplication(email);
        return toProfileDto(user, application);
    }

    @Override
    @Transactional
    public DriverProfileDto updateProfile(UpdateDriverProfileDto dto) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        User user = resolveUser(email);
        DriverApplication application = resolveApplication(email);

        if (!dto.getNicNumber().equals(application.getNicNumber())
                && driverApplicationRepository.existsByNicNumber(dto.getNicNumber())) {
            throw new CustomException("NIC number is already in use", 409);
        }

        String trimmedName = dto.getFullName().trim();
        application.setFullName(trimmedName);
        application.setDateOfBirth(dto.getDateOfBirth());
        application.setPhone(dto.getPhone());
        application.setWhatsapp(dto.getWhatsapp());
        application.setNicNumber(dto.getNicNumber());
        application.setLanguagesSpoken(String.join(",", dto.getLanguagesSpoken()));
        application.setYearsOfExperience(dto.getYearsOfExperience());
        application.setUpdatedAt(LocalDateTime.now());
        driverApplicationRepository.save(application);

        String[] nameParts = trimmedName.split("\\s+", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.debug("Driver {} updated profile", email);
        return toProfileDto(user, application);
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Driver not found", 404));
    }

    private DriverApplication resolveApplication(String email) {
        return driverApplicationRepository.findByUser_Email(email)
                .orElseThrow(() -> new CustomException("Driver profile not found", 404));
    }

    private DriverProfileDto toProfileDto(User user, DriverApplication application) {
        DriverProfileDto dto = new DriverProfileDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setMemberSinceYear(user.getCreatedAt() != null ? user.getCreatedAt().getYear() : null);
        dto.setIsVerified(Boolean.TRUE.equals(user.getIsActive()));
        dto.setRating(tourAssignmentRepository.getAverageRatingByDriverEmail(user.getEmail()));
        dto.setLifetimeTours(tourAssignmentRepository.countByDriver_Email(user.getEmail()));
        dto.setOnTimeRate(null);
        dto.setDateOfBirth(application.getDateOfBirth());
        dto.setPhone(application.getPhone());
        dto.setWhatsapp(application.getWhatsapp());
        dto.setNicNumber(application.getNicNumber());
        dto.setLanguagesSpoken(parseLanguages(application.getLanguagesSpoken()));
        dto.setYearsOfExperience(application.getYearsOfExperience());
        dto.setAvailability(application.getAvailability() != null
                ? application.getAvailability().name() : null);

        if (application.getDocuments() != null) {
            List<DriverProfileDocumentDto> docs = application.getDocuments().stream()
                    .map(this::toDocumentDto).collect(Collectors.toList());
            dto.setDocuments(docs);
        }

        return dto;
    }

    private DriverProfileDocumentDto toDocumentDto(DriverDocument doc) {
        DriverProfileDocumentDto dto = new DriverProfileDocumentDto();
        dto.setDocumentType(doc.getDocumentType().name());
        dto.setFileName(doc.getFileName());
        dto.setFilePath(doc.getFilePath());
        dto.setUploadedAt(doc.getUploadedAt());
        return dto;
    }

    private List<String> parseLanguages(String languagesSpoken) {
        if (languagesSpoken == null || languagesSpoken.isBlank()) {
            return List.of();
        }
        return Arrays.stream(languagesSpoken.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
