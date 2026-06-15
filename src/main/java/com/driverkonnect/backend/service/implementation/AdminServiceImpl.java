package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.admin.AdminRejectRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminDocumentDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverSummaryDto;
import com.driverkonnect.backend.entity.DriverApplication;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.enums.ApplicationStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.DriverApplicationRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final DriverApplicationRepository driverApplicationRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public List<AdminDriverSummaryDto> getAllApplications() {
        log.debug("Fetching all driver applications");
        return driverApplicationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional
    public AdminDriverResponseDto getApplication(Long applicationId) {
        log.debug("Fetching application ID: {}", applicationId);
        DriverApplication application = driverApplicationRepository.findByIdWithDocuments(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", 404));
        return toDetailDto(application);
    }

    @Override
    @Transactional
    public AdminDriverResponseDto approveApplication(Long applicationId) {
        DriverApplication application = driverApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", 404));

        if (application.getStatus() == ApplicationStatus.APPROVED) {
            throw new CustomException("Application is already approved", 400);
        }
        if (application.getStatus() == ApplicationStatus.DRAFT) {
            throw new CustomException("Cannot approve an incomplete application", 400);
        }

        application.setStatus(ApplicationStatus.APPROVED);
        application.setUpdatedAt(LocalDateTime.now());
        application = driverApplicationRepository.save(application);
        log.debug("Application ID: {} approved", applicationId);

        return toDetailDto(application);
    }

    @Override
    @Transactional
    public AdminDriverResponseDto rejectApplication(Long applicationId, AdminRejectRequestDto request) {
        DriverApplication application = driverApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", 404));

        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new CustomException("Application is already rejected", 400);
        }
        if (application.getStatus() == ApplicationStatus.DRAFT) {
            throw new CustomException("Cannot reject an incomplete application", 400);
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(request.getReason());
        application.setUpdatedAt(LocalDateTime.now());
        application = driverApplicationRepository.save(application);
        log.debug("Application ID: {} rejected — reason: {}", applicationId, request.getReason());

        return toDetailDto(application);
    }

    @Override
    @Transactional
    public AdminDriverResponseDto deactivateDriver(Long applicationId) {
        DriverApplication application = driverApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", 404));

        User user = application.getUser();
        if (!user.getIsActive()) {
            throw new CustomException("Driver account is already deactivated", 400);
        }

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenService.deleteKeyByUsername(user.getUsername());
        log.debug("Driver account deactivated for application ID: {}, user: {}", applicationId, user.getEmail());

        return toDetailDto(application);
    }

    private AdminDriverSummaryDto toSummaryDto(DriverApplication application) {
        AdminDriverSummaryDto dto = new AdminDriverSummaryDto();
        dto.setId(application.getId());
        dto.setApplicationNumber(application.getApplicationNumber());
        dto.setStatus(application.getStatus().name());
        dto.setFullName(application.getFullName());
        dto.setEmail(application.getUser().getEmail());
        dto.setPhone(application.getPhone());
        dto.setNicNumber(application.getNicNumber());
        dto.setIsActive(application.getUser().getIsActive());
        dto.setSubmittedAt(application.getSubmittedAt());
        dto.setCreatedAt(application.getCreatedAt());
        return dto;
    }

    private AdminDriverResponseDto toDetailDto(DriverApplication application) {
        User user = application.getUser();
        AdminDriverResponseDto dto = new AdminDriverResponseDto();
        dto.setId(application.getId());
        dto.setApplicationNumber(application.getApplicationNumber());
        dto.setStatus(application.getStatus().name());
        dto.setRejectionReason(application.getRejectionReason());
        dto.setFullName(application.getFullName());
        dto.setDateOfBirth(application.getDateOfBirth());
        dto.setPhone(application.getPhone());
        dto.setWhatsapp(application.getWhatsapp());
        dto.setNicNumber(application.getNicNumber());
        dto.setLanguagesSpoken(List.of(application.getLanguagesSpoken().split(",")));
        dto.setYearsOfExperience(application.getYearsOfExperience());
        dto.setAvailability(application.getAvailability().name());
        dto.setPrivacyPolicyAccepted(application.getPrivacyPolicyAccepted());
        dto.setPrivacyPolicyVersion(application.getPrivacyPolicyVersion());
        dto.setPrivacyPolicyAcceptedAt(application.getPrivacyPolicyAcceptedAt());
        dto.setUserId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());
        dto.setSubmittedAt(application.getSubmittedAt());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setUpdatedAt(application.getUpdatedAt());

        if (application.getDocuments() != null) {
            dto.setDocuments(application.getDocuments().stream()
                    .map(doc -> new AdminDocumentDto(
                            doc.getId(),
                            doc.getDocumentType().name(),
                            doc.getFileName(),
                            doc.getUploadedAt()))
                    .toList());
        }

        return dto;
    }
}
