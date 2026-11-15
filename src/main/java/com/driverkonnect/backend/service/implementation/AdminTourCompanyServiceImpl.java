package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.admin.AdminRejectRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminTourCompanyResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminTourCompanySummaryDto;
import com.driverkonnect.backend.entity.TourCompanyProfile;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.enums.TourCompanyStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.generics.PagedResponseDto;
import com.driverkonnect.backend.repository.TourCompanyProfileRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.AdminTourCompanyService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTourCompanyServiceImpl implements AdminTourCompanyService {

    private final TourCompanyProfileRepository tourCompanyProfileRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public PagedResponseDto<AdminTourCompanySummaryDto> getAllCompanies(TourCompanyStatus status, int page, int size) {
        log.debug("Fetching tour companies - status={}, page={}, size={}", status, page, size);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TourCompanyProfile> companies = status != null
                ? tourCompanyProfileRepository.findByStatusOrderByCreatedAtDesc(status, pageRequest)
                : tourCompanyProfileRepository.findAllByOrderByCreatedAtDesc(pageRequest);

        return new PagedResponseDto<>(
                companies.getContent().stream().map(this::toSummaryDto).toList(),
                companies.getNumber(), companies.getSize(),
                companies.getTotalElements(), companies.getTotalPages());
    }

    @Override
    @Transactional
    public AdminTourCompanyResponseDto getCompany(Long companyId) {
        log.debug("Fetching tour company ID: {}", companyId);
        return toDetailDto(findCompany(companyId));
    }

    @Override
    @Transactional
    public AdminTourCompanyResponseDto approveCompany(Long companyId) {
        TourCompanyProfile company = findCompany(companyId);

        switch (company.getStatus()) {
            case APPROVED -> throw new CustomException("Company is already approved", 400);
            case DEACTIVATED -> throw new CustomException("Company is deactivated. Use reactivate instead", 400);
            default -> { /* PENDING or REJECTED may be approved */ }
        }

        setUserActive(company.getUser(), true);
        company.setStatus(TourCompanyStatus.APPROVED);
        company.setRejectionReason(null);
        markReviewed(company);
        company = tourCompanyProfileRepository.save(company);
        log.debug("Tour company ID: {} approved", companyId);

        return toDetailDto(company);
    }

    @Override
    @Transactional
    public AdminTourCompanyResponseDto rejectCompany(Long companyId, AdminRejectRequestDto request) {
        TourCompanyProfile company = findCompany(companyId);

        if (company.getStatus() != TourCompanyStatus.PENDING) {
            throw new CustomException("Only pending registrations can be rejected (current status: "
                    + company.getStatus() + ")", 400);
        }

        company.setStatus(TourCompanyStatus.REJECTED);
        company.setRejectionReason(request.getReason() != null && !request.getReason().isBlank()
                ? request.getReason().strip() : null);
        markReviewed(company);
        company = tourCompanyProfileRepository.save(company);
        log.debug("Tour company ID: {} rejected - reason: {}", companyId, company.getRejectionReason());

        return toDetailDto(company);
    }

    @Override
    @Transactional
    public AdminTourCompanyResponseDto deactivateCompany(Long companyId) {
        TourCompanyProfile company = findCompany(companyId);

        if (company.getStatus() != TourCompanyStatus.APPROVED) {
            throw new CustomException("Only approved companies can be deactivated (current status: "
                    + company.getStatus() + ")", 400);
        }

        User user = company.getUser();
        // Revoke tokens first: deleteKeyByUsername only resolves active users.
        refreshTokenService.deleteKeyByUsername(user.getUsername());
        setUserActive(user, false);

        company.setStatus(TourCompanyStatus.DEACTIVATED);
        markReviewed(company);
        company = tourCompanyProfileRepository.save(company);
        log.debug("Tour company ID: {} deactivated, user: {}", companyId, user.getEmail());

        return toDetailDto(company);
    }

    @Override
    @Transactional
    public AdminTourCompanyResponseDto reactivateCompany(Long companyId) {
        TourCompanyProfile company = findCompany(companyId);

        if (company.getStatus() != TourCompanyStatus.DEACTIVATED) {
            throw new CustomException("Only deactivated companies can be reactivated (current status: "
                    + company.getStatus() + ")", 400);
        }

        setUserActive(company.getUser(), true);
        company.setStatus(TourCompanyStatus.APPROVED);
        markReviewed(company);
        company = tourCompanyProfileRepository.save(company);
        log.debug("Tour company ID: {} reactivated", companyId);

        return toDetailDto(company);
    }

    private TourCompanyProfile findCompany(Long companyId) {
        return tourCompanyProfileRepository.findById(companyId)
                .orElseThrow(() -> new CustomException("Tour company not found", 404));
    }

    private void setUserActive(User user, boolean active) {
        user.setIsActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void markReviewed(TourCompanyProfile company) {
        String adminEmail = AuthUtil.getAuthenticatedUser().getUsername();
        company.setReviewedBy(userRepository.findByEmail(adminEmail).orElse(null));
        company.setReviewedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
    }

    private AdminTourCompanySummaryDto toSummaryDto(TourCompanyProfile company) {
        return AdminTourCompanySummaryDto.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .businessRegistrationNumber(company.getBusinessRegistrationNumber())
                .contactPersonFirstName(company.getContactPersonFirstName())
                .contactPersonLastName(company.getContactPersonLastName())
                .email(company.getUser().getEmail())
                .phone(company.getPhone())
                .status(company.getStatus().name())
                .isActive(company.getUser().getIsActive())
                .createdAt(company.getCreatedAt())
                .build();
    }

    private AdminTourCompanyResponseDto toDetailDto(TourCompanyProfile company) {
        User user = company.getUser();
        return AdminTourCompanyResponseDto.builder()
                .id(company.getId())
                .userId(user.getId())
                .companyName(company.getCompanyName())
                .businessRegistrationNumber(company.getBusinessRegistrationNumber())
                .contactPersonFirstName(company.getContactPersonFirstName())
                .contactPersonLastName(company.getContactPersonLastName())
                .email(user.getEmail())
                .phone(company.getPhone())
                .status(company.getStatus().name())
                .isActive(user.getIsActive())
                .rejectionReason(company.getRejectionReason())
                .reviewedAt(company.getReviewedAt())
                .reviewedByEmail(company.getReviewedBy() != null ? company.getReviewedBy().getEmail() : null)
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
