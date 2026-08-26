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
        List<DriverProfileDocumentDto> docs = application.getDocuments() != null
                ? application.getDocuments().stream().map(this::toDocumentDto).collect(Collectors.toList())
                : null;
        return DriverProfileDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .memberSinceYear(user.getCreatedAt() != null ? user.getCreatedAt().getYear() : null)
                .isVerified(Boolean.TRUE.equals(user.getIsActive()))
                .rating(tourAssignmentRepository.getAverageRatingByDriverEmail(user.getEmail()))
                .lifetimeTours(tourAssignmentRepository.countByDriver_Email(user.getEmail()))
                .onTimeRate(null)
                .dateOfBirth(application.getDateOfBirth())
                .phone(application.getPhone())
                .whatsapp(application.getWhatsapp())
                .nicNumber(application.getNicNumber())
                .languagesSpoken(parseLanguages(application.getLanguagesSpoken()))
                .yearsOfExperience(application.getYearsOfExperience())
                .availability(application.getAvailability() != null ? application.getAvailability().name() : null)
                .documents(docs)
                .build();
    }

    private DriverProfileDocumentDto toDocumentDto(DriverDocument doc) {
        return DriverProfileDocumentDto.builder()
                .documentType(doc.getDocumentType().name())
                .fileName(doc.getFileName())
                .filePath(doc.getFilePath())
                .uploadedAt(doc.getUploadedAt())
                .build();
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
