package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.driver.ApplyForTourDto;
import com.driverkonnect.backend.dto.response.driver.DriverActiveTourDto;
import com.driverkonnect.backend.dto.response.driver.DriverDashboardDto;
import com.driverkonnect.backend.dto.response.driver.DriverHistorySummaryDto;
import com.driverkonnect.backend.dto.response.driver.DriverTourHistoryItemDto;
import com.driverkonnect.backend.dto.response.driver.TourApplicationResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourLocationResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.entity.DriverVehicle;
import com.driverkonnect.backend.entity.TourAssignment;
import com.driverkonnect.backend.entity.TourDriverApplication;
import com.driverkonnect.backend.entity.TourRequest;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.enums.TourStatus;
import com.driverkonnect.backend.enums.VehicleApprovalStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.generics.PagedResponseDto;
import com.driverkonnect.backend.repository.DriverVehicleRepository;
import com.driverkonnect.backend.repository.TourAssignmentRepository;
import com.driverkonnect.backend.repository.TourDriverApplicationRepository;
import com.driverkonnect.backend.repository.TourRequestRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.TourApplicationService;
import com.driverkonnect.backend.util.AuthUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourApplicationServiceImpl implements TourApplicationService {

    private final TourRequestRepository tourRequestRepository;
    private final TourDriverApplicationRepository tourDriverApplicationRepository;
    private final UserRepository userRepository;
    private final DriverVehicleRepository driverVehicleRepository;
    private final TourAssignmentRepository tourAssignmentRepository;

    @Override
    @Transactional
    public List<TourRequestSummaryDto> getPublishedTours() {
        return tourRequestRepository.findByStatusOrderByCreatedAtDesc(TourStatus.PUBLISHED)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional
    public TourRequestResponseDto getPublishedTourById(Long tourRequestId) {
        TourRequest tourRequest = tourRequestRepository.findById(tourRequestId)
                .orElseThrow(() -> new CustomException("Tour request not found", 404));

        if (tourRequest.getStatus() != TourStatus.PUBLISHED) {
            throw new CustomException("Tour request not found", 404);
        }

        return toResponseDto(tourRequest);
    }

    @Override
    @Transactional
    public TourApplicationResponseDto apply(Long tourRequestId, ApplyForTourDto dto) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        User driver = resolveDriver(email);

        TourRequest tourRequest = tourRequestRepository.findById(tourRequestId)
                .orElseThrow(() -> new CustomException("Tour request not found", 404));

        if (tourRequest.getStatus() != TourStatus.PUBLISHED) {
            throw new CustomException("Tour request is not available for applications", 400);
        }

        if (tourDriverApplicationRepository
                .existsByTourRequest_IdAndDriver_EmailAndIsWithdrawnFalse(tourRequestId, email)) {
            throw new CustomException("You have already applied for this tour", 409);
        }

        DriverVehicle vehicle = driverVehicleRepository
                .findByIdAndDriver_EmailAndIsActiveTrue(dto.getVehicleId(), email)
                .orElseThrow(() -> new CustomException("Vehicle not found", 404));

        if (vehicle.getApprovalStatus() != VehicleApprovalStatus.APPROVED) {
            throw new CustomException("Only approved vehicles can be used to apply for tours", 400);
        }

        TourDriverApplication application = new TourDriverApplication();
        application.setTourRequest(tourRequest);
        application.setDriver(driver);
        application.setDriverVehicle(vehicle);
        application.setPerKmRateSnapshot(vehicle.getPerKmRate());
        application.setNote(dto.getNote());
        application.setIsWithdrawn(false);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        application = tourDriverApplicationRepository.save(application);
        log.debug("Driver {} applied for tour request ID: {}", email, tourRequestId);
        return toApplicationDto(application);
    }

    @Override
    @Transactional
    public List<TourApplicationResponseDto> getMyApplications() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        return tourDriverApplicationRepository.findByDriver_EmailOrderByAppliedAtDesc(email)
                .stream()
                .map(this::toApplicationDto)
                .toList();
    }

    @Override
    @Transactional
    public TourApplicationResponseDto withdraw(Long applicationId) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        TourDriverApplication application = tourDriverApplicationRepository
                .findByIdAndDriver_Email(applicationId, email)
                .orElseThrow(() -> new CustomException("Application not found", 404));

        if (application.getIsWithdrawn()) {
            throw new CustomException("Application is already withdrawn", 400);
        }

        application.setIsWithdrawn(true);
        application.setUpdatedAt(LocalDateTime.now());
        application = tourDriverApplicationRepository.save(application);
        log.debug("Driver {} withdrew application ID: {}", email, applicationId);
        return toApplicationDto(application);
    }

    @Override
    @Transactional
    public DriverDashboardDto getDashboard() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        User driver = resolveDriver(email);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        long assignedCount = tourAssignmentRepository.countByDriver_Email(email);
        long totalApplications = tourDriverApplicationRepository.countByDriver_EmailAndIsWithdrawnFalse(email);
        Double acceptanceRate = totalApplications > 0
                ? Math.round((double) assignedCount / totalApplications * 1000.0) / 10.0
                : null;

        List<TourRequestSummaryDto> newTours = tourRequestRepository
                .findByStatusOrderByCreatedAtDesc(TourStatus.PUBLISHED)
                .stream().limit(5).map(this::toSummaryDto).toList();

        DriverDashboardDto dto = new DriverDashboardDto();
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setIsVerified(Boolean.TRUE.equals(driver.getIsActive()));
        dto.setRating(tourAssignmentRepository.getAverageRatingByDriverEmail(email));
        dto.setWeeklyToursCount(tourAssignmentRepository.countAssignmentsInWeek(email, weekStart, weekEnd));
        dto.setAcceptanceRate(acceptanceRate);
        dto.setOnTimeRate(null);
        dto.setNewTourRequests(newTours);
        return dto;
    }

    @Override
    @Transactional
    public DriverHistorySummaryDto getMyHistory(int page, int size) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        List<TourStatus> historyStatuses = List.of(TourStatus.COMPLETED, TourStatus.CANCELLED);

        // Paginated list for display
        Page<TourAssignment> assignmentPage =
                tourAssignmentRepository.findByDriver_EmailAndTourRequest_StatusInOrderByAssignedAtDesc(
                        email, historyStatuses, PageRequest.of(page, size));

        List<DriverTourHistoryItemDto> items = assignmentPage.getContent()
                .stream().map(this::toHistoryItemDto).toList();

        PagedResponseDto<DriverTourHistoryItemDto> pagedTours = new PagedResponseDto<>(
                items, assignmentPage.getNumber(), assignmentPage.getSize(),
                assignmentPage.getTotalElements(), assignmentPage.getTotalPages());

        // Stats — computed from all COMPLETED records only
        List<TourAssignment> completed = tourAssignmentRepository
                .findByDriver_EmailAndTourRequest_Status(email, TourStatus.COMPLETED);

        BigDecimal lifetimeEarned = completed.stream()
                .filter(a -> a.getPerKmRateSnapshot() != null && a.getTourRequest().getEstimatedKm() != null)
                .map(a -> a.getPerKmRateSnapshot().multiply(a.getTourRequest().getEstimatedKm()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OptionalDouble avgRating = completed.stream()
                .filter(a -> a.getRating() != null)
                .mapToInt(TourAssignment::getRating)
                .average();

        DriverHistorySummaryDto summary = new DriverHistorySummaryDto();
        summary.setCompletedCount(completed.size());
        summary.setLifetimeEarned(lifetimeEarned);
        summary.setAverageRating(avgRating.isPresent() ? avgRating.getAsDouble() : null);
        summary.setTours(pagedTours);
        return summary;
    }

    @Override
    @Transactional
    public DriverActiveTourDto getMyActiveTour() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        TourAssignment assignment = tourAssignmentRepository
                .findActiveByDriverEmail(email, List.of(TourStatus.ASSIGNED, TourStatus.IN_PROGRESS))
                .orElseThrow(() -> new CustomException("No active tour found", 404));
        return toActiveTourDto(assignment);
    }

    @Override
    @Transactional
    public DriverActiveTourDto startTour(Long tourRequestId) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        TourAssignment assignment = tourAssignmentRepository
                .findByDriver_EmailAndTourRequest_Id(email, tourRequestId)
                .orElseThrow(() -> new CustomException("Tour assignment not found", 404));

        TourRequest tourRequest = assignment.getTourRequest();
        if (tourRequest.getStatus() != TourStatus.ASSIGNED) {
            throw new CustomException("Tour must be in ASSIGNED status to be started", 400);
        }

        tourRequest.setStatus(TourStatus.IN_PROGRESS);
        tourRequest.setUpdatedAt(LocalDateTime.now());
        tourRequestRepository.save(tourRequest);

        log.debug("Driver {} started tour request ID: {}", email, tourRequestId);
        return toActiveTourDto(assignment);
    }

    private User resolveDriver(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Driver account not found", 404));
    }

    private TourApplicationResponseDto toApplicationDto(TourDriverApplication a) {
        TourApplicationResponseDto dto = new TourApplicationResponseDto();
        dto.setId(a.getId());
        dto.setTourRequestId(a.getTourRequest().getId());
        dto.setTourName(a.getTourRequest().getTourName());
        dto.setStartDate(a.getTourRequest().getStartDate());
        dto.setEndDate(a.getTourRequest().getEndDate());
        dto.setVehicleTypeName(a.getTourRequest().getVehicleType().getName());
        if (a.getDriverVehicle() != null) {
            dto.setVehicleId(a.getDriverVehicle().getId());
            dto.setVehicleBrand(a.getDriverVehicle().getBrand());
            dto.setVehicleModel(a.getDriverVehicle().getModel());
            dto.setVehicleCategory(a.getDriverVehicle().getVehicleCategory().name());
        }
        dto.setPerKmRateSnapshot(a.getPerKmRateSnapshot());
        dto.setNote(a.getNote());
        dto.setStatusLabel(a.getTourApplicationStatus() != null
                ? a.getTourApplicationStatus().getLabel()
                : null);
        dto.setIsWithdrawn(a.getIsWithdrawn());
        dto.setAppliedAt(a.getAppliedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }

    private TourRequestSummaryDto toSummaryDto(TourRequest t) {
        TourRequestSummaryDto dto = new TourRequestSummaryDto();
        dto.setId(t.getId());
        dto.setTourName(t.getTourName());
        dto.setTripType(t.getTripType().name());
        dto.setTravellerNationality(t.getTravellerNationality().name());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setPaxCount(t.getPaxCount());
        dto.setVehicleTypeName(t.getVehicleType().getName());
        dto.setStatus(t.getStatus().name());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    private DriverTourHistoryItemDto toHistoryItemDto(TourAssignment a) {
        TourRequest t = a.getTourRequest();
        DriverTourHistoryItemDto dto = new DriverTourHistoryItemDto();
        dto.setTourRequestId(t.getId());
        dto.setTourName(t.getTourName());
        dto.setTripType(t.getTripType().name());
        dto.setStatus(t.getStatus().name());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setRating(a.getRating());
        if (a.getPerKmRateSnapshot() != null && t.getEstimatedKm() != null) {
            dto.setEstimatedEarnings(a.getPerKmRateSnapshot().multiply(t.getEstimatedKm()));
        }
        return dto;
    }

    private DriverActiveTourDto toActiveTourDto(TourAssignment assignment) {
        TourRequest t = assignment.getTourRequest();
        DriverActiveTourDto dto = new DriverActiveTourDto();
        dto.setTourRequestId(t.getId());
        dto.setTourName(t.getTourName());
        dto.setTripType(t.getTripType().name());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setDays(t.getDays());
        dto.setNights(t.getNights());
        dto.setPaxCount(t.getPaxCount());
        dto.setTravellerNationality(t.getTravellerNationality().name());
        dto.setVehicleTypeName(t.getVehicleType().getName());
        dto.setEstimatedKm(t.getEstimatedKm());
        dto.setPaymentTerm(t.getPaymentTerm().name());
        dto.setSpecificRequirements(t.getSpecificRequirements());
        dto.setSpecialConcerns(t.getSpecialConcerns());
        dto.setStatus(t.getStatus().name());
        dto.setPerKmRateSnapshot(assignment.getPerKmRateSnapshot());
        if (assignment.getPerKmRateSnapshot() != null && t.getEstimatedKm() != null) {
            dto.setEstimatedEarnings(assignment.getPerKmRateSnapshot().multiply(t.getEstimatedKm()));
        }
        dto.setAssignedAt(assignment.getAssignedAt());

        if (t.getLocations() != null) {
            dto.setLocations(t.getLocations().stream().map(loc -> {
                TourLocationResponseDto locDto = new TourLocationResponseDto();
                locDto.setId(loc.getId());
                locDto.setLocationType(loc.getLocationType().name());
                locDto.setAddress(loc.getAddress());
                locDto.setLatitude(loc.getLatitude());
                locDto.setLongitude(loc.getLongitude());
                locDto.setSequenceOrder(loc.getSequenceOrder());
                return locDto;
            }).toList());
        }

        return dto;
    }

    private TourRequestResponseDto toResponseDto(TourRequest t) {
        TourRequestResponseDto dto = new TourRequestResponseDto();
        dto.setId(t.getId());
        dto.setTourName(t.getTourName());
        dto.setTripType(t.getTripType().name());
        dto.setTravellerNationality(t.getTravellerNationality().name());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setDays(t.getDays());
        dto.setNights(t.getNights());
        dto.setPaxCount(t.getPaxCount());
        dto.setVehicleTypeId(t.getVehicleType().getId());
        dto.setVehicleTypeName(t.getVehicleType().getName());
        dto.setEstimatedKm(t.getEstimatedKm());
        dto.setSpecificRequirements(t.getSpecificRequirements());
        dto.setSpecialConcerns(t.getSpecialConcerns());
        dto.setPaymentTerm(t.getPaymentTerm().name());
        dto.setStatus(t.getStatus().name());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());

        if (t.getLocations() != null) {
            dto.setLocations(t.getLocations().stream().map(loc -> {
                TourLocationResponseDto locDto = new TourLocationResponseDto();
                locDto.setId(loc.getId());
                locDto.setLocationType(loc.getLocationType().name());
                locDto.setAddress(loc.getAddress());
                locDto.setLatitude(loc.getLatitude());
                locDto.setLongitude(loc.getLongitude());
                locDto.setSequenceOrder(loc.getSequenceOrder());
                return locDto;
            }).toList());
        }

        return dto;
    }
}
