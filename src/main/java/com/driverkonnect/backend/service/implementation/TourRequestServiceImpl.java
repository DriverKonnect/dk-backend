package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.tourcompany.TourLocationDto;
import com.driverkonnect.backend.dto.request.tourcompany.TourRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourLocationResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.entity.TourCompanyProfile;
import com.driverkonnect.backend.entity.TourLocation;
import com.driverkonnect.backend.entity.TourRequest;
import com.driverkonnect.backend.entity.VehicleType;
import com.driverkonnect.backend.enums.LocationType;
import com.driverkonnect.backend.enums.TourStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.TourCompanyProfileRepository;
import com.driverkonnect.backend.repository.TourRequestRepository;
import com.driverkonnect.backend.repository.VehicleTypeRepository;
import com.driverkonnect.backend.service.TourRequestService;
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
public class TourRequestServiceImpl implements TourRequestService {

    private final TourRequestRepository tourRequestRepository;
    private final TourCompanyProfileRepository tourCompanyProfileRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

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
    public List<TourRequestSummaryDto> getMyTours() {
        TourCompanyProfile company = resolveCurrentCompany();
        return tourRequestRepository.findByTourCompany_IdOrderByCreatedAtDesc(company.getId())
                .stream()
                .map(this::toSummaryDto)
                .toList();
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

        if (!hasPickup) {
            throw new CustomException("At least one pickup location is required", 400);
        }
        if (!hasDropoff) {
            throw new CustomException("At least one dropoff location is required", 400);
        }
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
}
