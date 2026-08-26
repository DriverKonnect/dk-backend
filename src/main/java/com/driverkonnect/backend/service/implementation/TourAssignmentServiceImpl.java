package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.tourcompany.TourApplicationDecisionDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourAssignmentResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourDriverApplicationSummaryDto;
import com.driverkonnect.backend.entity.*;
import com.driverkonnect.backend.enums.TourStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.*;
import com.driverkonnect.backend.service.TourAssignmentService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourAssignmentServiceImpl implements TourAssignmentService {

    private final TourRequestRepository tourRequestRepository;
    private final TourDriverApplicationRepository tourDriverApplicationRepository;
    private final TourApplicationStatusRepository tourApplicationStatusRepository;
    private final TourAssignmentRepository tourAssignmentRepository;
    private final TourCompanyProfileRepository tourCompanyProfileRepository;

    @Override
    @Transactional
    public List<TourDriverApplicationSummaryDto> getApplicationsForTour(Long tourRequestId) {
        TourCompanyProfile company = resolveCurrentCompany();
        findOwnedTour(tourRequestId, company.getId());

        return tourDriverApplicationRepository.findByTourRequest_IdOrderByAppliedAtDesc(tourRequestId)
                .stream()
                .map(this::toApplicationSummaryDto)
                .toList();
    }

    @Override
    @Transactional
    public TourDriverApplicationSummaryDto updateApplicationStatus(
            Long tourRequestId, Long applicationId, TourApplicationDecisionDto dto) {
        TourCompanyProfile company = resolveCurrentCompany();
        findOwnedTour(tourRequestId, company.getId());

        TourDriverApplication application = findApplication(applicationId, tourRequestId);

        if (application.getIsWithdrawn()) {
            throw new CustomException("Cannot update status of a withdrawn application", 400);
        }

        TourApplicationStatus status = tourApplicationStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new CustomException("Application status not found", 404));
        if (!status.getIsActive()) {
            throw new CustomException("Selected application status is not active", 400);
        }

        application.setTourApplicationStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        application = tourDriverApplicationRepository.save(application);
        log.debug("Updated application ID: {} to status: {}", applicationId, status.getLabel());
        return toApplicationSummaryDto(application);
    }

    @Override
    @Transactional
    public TourAssignmentResponseDto acceptApplication(Long tourRequestId, Long applicationId) {
        TourCompanyProfile company = resolveCurrentCompany();
        TourRequest tourRequest = findOwnedTour(tourRequestId, company.getId());

        if (tourRequest.getStatus() != TourStatus.PUBLISHED) {
            throw new CustomException("Tour must be published before a driver can be accepted", 400);
        }
        if (tourAssignmentRepository.existsByTourRequest_Id(tourRequestId)) {
            throw new CustomException("A driver has already been assigned to this tour", 409);
        }

        TourDriverApplication application = findApplication(applicationId, tourRequestId);

        if (application.getIsWithdrawn()) {
            throw new CustomException("Cannot accept a withdrawn application", 400);
        }

        TourAssignment assignment = new TourAssignment();
        assignment.setTourRequest(tourRequest);
        assignment.setDriver(application.getDriver());
        assignment.setPerKmRateSnapshot(application.getPerKmRateSnapshot());
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment = tourAssignmentRepository.save(assignment);

        tourRequest.setStatus(TourStatus.ASSIGNED);
        tourRequest.setUpdatedAt(LocalDateTime.now());
        tourRequestRepository.save(tourRequest);

        log.debug("Accepted application ID: {} for tour ID: {}, created assignment ID: {}",
                applicationId, tourRequestId, assignment.getId());
        return toAssignmentDto(assignment);
    }

    private TourCompanyProfile resolveCurrentCompany() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        return tourCompanyProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new CustomException("Tour company profile not found", 404));
    }

    private TourRequest findOwnedTour(Long tourRequestId, Long companyId) {
        return tourRequestRepository.findByIdAndTourCompany_Id(tourRequestId, companyId)
                .orElseThrow(() -> new CustomException("Tour request not found", 404));
    }

    private TourDriverApplication findApplication(Long applicationId, Long tourRequestId) {
        TourDriverApplication application = tourDriverApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", 404));
        if (!application.getTourRequest().getId().equals(tourRequestId)) {
            throw new CustomException("Application does not belong to this tour", 404);
        }
        return application;
    }

    private TourDriverApplicationSummaryDto toApplicationSummaryDto(TourDriverApplication a) {
        User driver = a.getDriver();
        DriverVehicle v = a.getDriverVehicle();
        return TourDriverApplicationSummaryDto.builder()
                .id(a.getId())
                .driverId(driver.getId())
                .driverFirstName(driver.getFirstName())
                .driverLastName(driver.getLastName())
                .driverEmail(driver.getEmail())
                .vehicleId(v != null ? v.getId() : null)
                .vehicleBrand(v != null ? v.getBrand() : null)
                .vehicleModel(v != null ? v.getModel() : null)
                .vehicleCategory(v != null ? v.getVehicleCategory().name() : null)
                .perKmRateSnapshot(a.getPerKmRateSnapshot())
                .note(a.getNote())
                .statusLabel(a.getTourApplicationStatus() != null ? a.getTourApplicationStatus().getLabel() : null)
                .isWithdrawn(a.getIsWithdrawn())
                .appliedAt(a.getAppliedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private TourAssignmentResponseDto toAssignmentDto(TourAssignment a) {
        User driver = a.getDriver();
        return TourAssignmentResponseDto.builder()
                .id(a.getId())
                .tourRequestId(a.getTourRequest().getId())
                .tourName(a.getTourRequest().getTourName())
                .driverId(driver.getId())
                .driverFirstName(driver.getFirstName())
                .driverLastName(driver.getLastName())
                .driverEmail(driver.getEmail())
                .assignedAt(a.getAssignedAt())
                .build();
    }
}
