package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.tourcompany.TourLocationDto;
import com.driverkonnect.backend.dto.request.tourcompany.TourRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourLocationResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.entity.*;
import com.driverkonnect.backend.enums.LocationType;
import com.driverkonnect.backend.enums.TourStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.generics.PagedResponseDto;
import com.driverkonnect.backend.repository.*;
import com.driverkonnect.backend.service.TourRequestService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourRequestServiceImpl implements TourRequestService {

    private final TourRequestRepository tourRequestRepository;
    private final TourCompanyProfileRepository tourCompanyProfileRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final TourAssignmentRepository tourAssignmentRepository;

    @Override
    @Transactional
    public TourRequestResponseDto create(TourRequestDto dto) {
        TourCompanyProfile company = resolveCurrentCompany();

        validateDates(dto);
        validateLocations(dto.getLocations());
        VehicleType vehicleType = resolveVehicleType(dto.getVehicleTypeId());

        TourRequest tourRequest = new TourRequest();
        tourRequest.setTourCompany(company);
        tourRequest.setStatus(TourStatus.DRAFT);
        tourRequest.setCreatedAt(LocalDateTime.now());
        tourRequest.setUpdatedAt(LocalDateTime.now());
        applyFields(tourRequest, dto, vehicleType);

        List<TourLocation> locations = buildLocations(dto.getLocations(), tourRequest);
        tourRequest.setLocations(locations);

        tourRequest = tourRequestRepository.save(tourRequest);
        log.debug("Created tour request ID: {} for company ID: {}", tourRequest.getId(), company.getId());
        return toResponseDto(tourRequest);
    }

    @Override
    @Transactional
    public PagedResponseDto<TourRequestSummaryDto> getMyTours(
            TourStatus status, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        TourCompanyProfile company = resolveCurrentCompany();

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TourRequest> tourPage = tourRequestRepository.findByCompanyWithFilters(
                company.getId(), status, dateFrom, dateTo, pageable);

        List<Long> tourIds = tourPage.getContent().stream().map(TourRequest::getId).toList();
        Map<Long, TourAssignment> assignmentMap = tourAssignmentRepository
                .findByTourRequest_IdIn(tourIds)
                .stream()
                .collect(Collectors.toMap(a -> a.getTourRequest().getId(), a -> a));

        List<TourRequestSummaryDto> content = tourPage.getContent().stream()
                .map(t -> toSummaryDto(t, assignmentMap.get(t.getId())))
                .toList();

        return new PagedResponseDto<>(content, tourPage.getNumber(), tourPage.getSize(),
                tourPage.getTotalElements(), tourPage.getTotalPages());
    }

    @Override
    @Transactional
    public TourRequestResponseDto getById(Long id) {
        TourCompanyProfile company = resolveCurrentCompany();
        TourRequest tourRequest = findOwned(id, company.getId());
        return toResponseDto(tourRequest);
    }

    @Override
    @Transactional
    public TourRequestResponseDto update(Long id, TourRequestDto dto) {
        TourCompanyProfile company = resolveCurrentCompany();
        TourRequest tourRequest = findOwned(id, company.getId());

        if (tourRequest.getStatus() != TourStatus.DRAFT) {
            throw new CustomException("Only draft tour requests can be updated", 400);
        }

        validateDates(dto);
        validateLocations(dto.getLocations());
        VehicleType vehicleType = resolveVehicleType(dto.getVehicleTypeId());

        applyFields(tourRequest, dto, vehicleType);

        tourRequest.getLocations().clear();
        tourRequest.getLocations().addAll(buildLocations(dto.getLocations(), tourRequest));
        tourRequest.setUpdatedAt(LocalDateTime.now());

        tourRequest = tourRequestRepository.save(tourRequest);
        log.debug("Updated tour request ID: {}", id);
        return toResponseDto(tourRequest);
    }

    @Override
    @Transactional
    public TourRequestResponseDto publish(Long id) {
        TourCompanyProfile company = resolveCurrentCompany();
        TourRequest tourRequest = findOwned(id, company.getId());

        if (tourRequest.getStatus() != TourStatus.DRAFT) {
            throw new CustomException("Only draft tour requests can be published", 400);
        }

        tourRequest.setStatus(TourStatus.PUBLISHED);
        tourRequest.setUpdatedAt(LocalDateTime.now());
        tourRequest = tourRequestRepository.save(tourRequest);
        log.debug("Published tour request ID: {}", id);
        return toResponseDto(tourRequest);
    }

    private TourCompanyProfile resolveCurrentCompany() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        return tourCompanyProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new CustomException("Tour company profile not found", 404));
    }

    private VehicleType resolveVehicleType(Long vehicleTypeId) {
        VehicleType vehicleType = vehicleTypeRepository.findById(vehicleTypeId)
                .orElseThrow(() -> new CustomException("Vehicle type not found", 404));
        if (!vehicleType.getIsActive()) {
            throw new CustomException("Selected vehicle type is not available", 400);
        }
        return vehicleType;
    }

    private TourRequest findOwned(Long id, Long companyId) {
        return tourRequestRepository.findByIdAndTourCompany_Id(id, companyId)
                .orElseThrow(() -> new CustomException("Tour request not found", 404));
    }

    private void validateDates(TourRequestDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new CustomException("End date must be on or after start date", 400);
        }
    }

    private void validateLocations(List<TourLocationDto> locations) {
        boolean hasPickup = locations.stream()
                .anyMatch(l -> l.getLocationType() == LocationType.PICKUP);
        boolean hasDropoff = locations.stream()
                .anyMatch(l -> l.getLocationType() == LocationType.DROPOFF);

        if (!hasPickup) throw new CustomException("At least one pickup location is required", 400);
        if (!hasDropoff) throw new CustomException("At least one dropoff location is required", 400);
    }

    private void applyFields(TourRequest tourRequest, TourRequestDto dto, VehicleType vehicleType) {
        tourRequest.setTourName(dto.getTourName().trim());
        tourRequest.setTripType(dto.getTripType());
        tourRequest.setTravellerNationality(dto.getTravellerNationality());
        tourRequest.setStartDate(dto.getStartDate());
        tourRequest.setEndDate(dto.getEndDate());
        tourRequest.setDays(dto.getDays());
        tourRequest.setNights(dto.getNights());
        tourRequest.setPaxCount(dto.getPaxCount());
        tourRequest.setVehicleType(vehicleType);
        tourRequest.setEstimatedKm(dto.getEstimatedKm());
        tourRequest.setSpecificRequirements(dto.getSpecificRequirements());
        tourRequest.setSpecialConcerns(dto.getSpecialConcerns());
        tourRequest.setPaymentTerm(dto.getPaymentTerm());
    }

    private List<TourLocation> buildLocations(List<TourLocationDto> dtos, TourRequest tourRequest) {
        return dtos.stream().map(dto -> {
            TourLocation location = new TourLocation();
            location.setTourRequest(tourRequest);
            location.setLocationType(dto.getLocationType());
            location.setAddress(dto.getAddress().trim());
            location.setLatitude(dto.getLatitude());
            location.setLongitude(dto.getLongitude());
            location.setSequenceOrder(dto.getSequenceOrder());
            location.setCreatedAt(LocalDateTime.now());
            return location;
        }).toList();
    }

    private TourRequestSummaryDto toSummaryDto(TourRequest t, TourAssignment assignment) {
        return TourRequestSummaryDto.builder()
                .id(t.getId())
                .referenceNumber(generateReferenceNumber(t))
                .tourName(t.getTourName())
                .tripType(t.getTripType().name())
                .travellerNationality(t.getTravellerNationality().name())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .paxCount(t.getPaxCount())
                .vehicleTypeName(t.getVehicleType().getName())
                .status(t.getStatus().name())
                .amount(t.getAmount())
                .paymentStatus(t.getPaymentStatus() != null ? t.getPaymentStatus().name() : null)
                .createdAt(t.getCreatedAt())
                .driverFirstName(assignment != null ? assignment.getDriver().getFirstName() : null)
                .driverLastName(assignment != null ? assignment.getDriver().getLastName() : null)
                .rating(assignment != null ? assignment.getRating() : null)
                .build();
    }

    private String generateReferenceNumber(TourRequest t) {
        int year = t.getCreatedAt() != null
                ? t.getCreatedAt().getYear()
                : LocalDate.now().getYear();
        return String.format("TR-%d-%04d", year, t.getId());
    }

    private TourRequestResponseDto toResponseDto(TourRequest t) {
        List<TourLocationResponseDto> locations = t.getLocations() != null
                ? t.getLocations().stream().map(loc -> TourLocationResponseDto.builder()
                        .id(loc.getId())
                        .locationType(loc.getLocationType().name())
                        .address(loc.getAddress())
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .sequenceOrder(loc.getSequenceOrder())
                        .build()).toList()
                : null;
        return TourRequestResponseDto.builder()
                .id(t.getId())
                .tourName(t.getTourName())
                .tripType(t.getTripType().name())
                .travellerNationality(t.getTravellerNationality().name())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .days(t.getDays())
                .nights(t.getNights())
                .paxCount(t.getPaxCount())
                .vehicleTypeId(t.getVehicleType().getId())
                .vehicleTypeName(t.getVehicleType().getName())
                .estimatedKm(t.getEstimatedKm())
                .specificRequirements(t.getSpecificRequirements())
                .specialConcerns(t.getSpecialConcerns())
                .paymentTerm(t.getPaymentTerm().name())
                .status(t.getStatus().name())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .locations(locations)
                .build();
    }
}
