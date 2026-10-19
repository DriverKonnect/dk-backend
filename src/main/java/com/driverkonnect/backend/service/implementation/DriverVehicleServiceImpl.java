package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.driver.DriverVehicleRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverVehicleSummaryDto;
import com.driverkonnect.backend.dto.response.driver.DriverVehicleResponseDto;
import com.driverkonnect.backend.entity.DriverVehicle;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.enums.VehicleApprovalStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.generics.PagedResponseDto;
import com.driverkonnect.backend.repository.DriverVehicleRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.service.DriverVehicleService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverVehicleServiceImpl implements DriverVehicleService {

    private final DriverVehicleRepository driverVehicleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DriverVehicleResponseDto addVehicle(DriverVehicleRequestDto dto) {
        User driver = resolveCurrentDriver();

        DriverVehicle vehicle = new DriverVehicle();
        vehicle.setDriver(driver);
        vehicle.setApprovalStatus(VehicleApprovalStatus.PENDING);
        vehicle.setIsActive(true);
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());
        applyFields(vehicle, dto);

        vehicle = driverVehicleRepository.save(vehicle);
        log.debug("Driver {} added vehicle ID: {}", driver.getEmail(), vehicle.getId());
        return toDriverResponseDto(vehicle);
    }

    @Override
    @Transactional
    public List<DriverVehicleResponseDto> getMyVehicles() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        return driverVehicleRepository.findByDriver_EmailAndIsActiveTrueOrderByCreatedAtDesc(email)
                .stream().map(this::toDriverResponseDto).toList();
    }

    @Override
    @Transactional
    public DriverVehicleResponseDto getMyVehicleById(Long id) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        DriverVehicle vehicle = driverVehicleRepository.findByIdAndDriver_EmailAndIsActiveTrue(id, email)
                .orElseThrow(() -> new CustomException("Vehicle not found", 404));
        return toDriverResponseDto(vehicle);
    }

    @Override
    @Transactional
    public DriverVehicleResponseDto updateVehicle(Long id, DriverVehicleRequestDto dto) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        DriverVehicle vehicle = driverVehicleRepository.findByIdAndDriver_EmailAndIsActiveTrue(id, email)
                .orElseThrow(() -> new CustomException("Vehicle not found", 404));

        applyFields(vehicle, dto);
        vehicle.setUpdatedAt(LocalDateTime.now());

        // Reset to PENDING so admin re-reviews any changes
        if (vehicle.getApprovalStatus() == VehicleApprovalStatus.APPROVED
                || vehicle.getApprovalStatus() == VehicleApprovalStatus.REJECTED) {
            vehicle.setApprovalStatus(VehicleApprovalStatus.PENDING);
            vehicle.setRejectionReason(null);
        }

        vehicle = driverVehicleRepository.save(vehicle);
        log.debug("Driver {} updated vehicle ID: {}", email, id);
        return toDriverResponseDto(vehicle);
    }

    @Override
    @Transactional
    public void removeVehicle(Long id) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        DriverVehicle vehicle = driverVehicleRepository.findByIdAndDriver_EmailAndIsActiveTrue(id, email)
                .orElseThrow(() -> new CustomException("Vehicle not found", 404));
        vehicle.setIsActive(false);
        vehicle.setUpdatedAt(LocalDateTime.now());
        driverVehicleRepository.save(vehicle);
        log.debug("Driver {} soft-deleted vehicle ID: {}", email, id);
    }

    @Override
    @Transactional
    public PagedResponseDto<AdminDriverVehicleSummaryDto> getAllVehicles(VehicleApprovalStatus status, int page, int size) {
        Page<DriverVehicle> vehiclePage = driverVehicleRepository.findAllActiveWithFilter(
                status, PageRequest.of(page, size));
        List<AdminDriverVehicleSummaryDto> content = vehiclePage.getContent()
                .stream().map(this::toAdminSummaryDto).toList();
        return new PagedResponseDto<>(content, vehiclePage.getNumber(), vehiclePage.getSize(),
                vehiclePage.getTotalElements(), vehiclePage.getTotalPages());
    }

    @Override
    @Transactional
    public AdminDriverVehicleSummaryDto approveVehicle(Long id) {
        DriverVehicle vehicle = driverVehicleRepository.findById(id)
                .filter(DriverVehicle::getIsActive)
                .orElseThrow(() -> new CustomException("Vehicle not found", 404));

        if (vehicle.getApprovalStatus() == VehicleApprovalStatus.APPROVED) {
            throw new CustomException("Vehicle is already approved", 409);
        }

        vehicle.setApprovalStatus(VehicleApprovalStatus.APPROVED);
        vehicle.setRejectionReason(null);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicle = driverVehicleRepository.save(vehicle);
        log.debug("Admin approved vehicle ID: {}", id);
        return toAdminSummaryDto(vehicle);
    }

    @Override
    @Transactional
    public AdminDriverVehicleSummaryDto rejectVehicle(Long id, String rejectionReason) {
        DriverVehicle vehicle = driverVehicleRepository.findById(id)
                .filter(DriverVehicle::getIsActive)
                .orElseThrow(() -> new CustomException("Vehicle not found", 404));

        if (vehicle.getApprovalStatus() == VehicleApprovalStatus.REJECTED) {
            throw new CustomException("Vehicle is already rejected", 409);
        }

        vehicle.setApprovalStatus(VehicleApprovalStatus.REJECTED);
        vehicle.setRejectionReason(rejectionReason);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicle = driverVehicleRepository.save(vehicle);
        log.debug("Admin rejected vehicle ID: {}", id);
        return toAdminSummaryDto(vehicle);
    }

    private User resolveCurrentDriver() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Driver not found", 404));
    }

    private void applyFields(DriverVehicle vehicle, DriverVehicleRequestDto dto) {
        vehicle.setBrand(dto.getBrand().trim());
        vehicle.setModel(dto.getModel().trim());
        vehicle.setYear(dto.getYear());
        vehicle.setMileageKm(dto.getMileageKm());
        vehicle.setVehicleCategory(dto.getVehicleCategory());
        vehicle.setPerKmRate(dto.getPerKmRate());
        vehicle.setInsuranceExpiry(dto.getInsuranceExpiry());
    }

    private DriverVehicleResponseDto toDriverResponseDto(DriverVehicle v) {
        return DriverVehicleResponseDto.builder()
                .id(v.getId())
                .brand(v.getBrand())
                .model(v.getModel())
                .year(v.getYear())
                .mileageKm(v.getMileageKm())
                .vehicleCategory(v.getVehicleCategory().name())
                .perKmRate(v.getPerKmRate())
                .approvalStatus(v.getApprovalStatus().name())
                .rejectionReason(v.getRejectionReason())
                .photoFrontUrl(v.getPhotoFrontUrl())
                .photoBackUrl(v.getPhotoBackUrl())
                .photoSideUrl(v.getPhotoSideUrl())
                .photoInteriorUrl(v.getPhotoInteriorUrl())
                .vehicleLicenceUrl(v.getVehicleLicenceUrl())
                .insuranceUrl(v.getInsuranceUrl())
                .insuranceExpiry(v.getInsuranceExpiry())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    private AdminDriverVehicleSummaryDto toAdminSummaryDto(DriverVehicle v) {
        return AdminDriverVehicleSummaryDto.builder()
                .id(v.getId())
                .driverId(v.getDriver().getId())
                .driverFirstName(v.getDriver().getFirstName())
                .driverLastName(v.getDriver().getLastName())
                .driverEmail(v.getDriver().getEmail())
                .brand(v.getBrand())
                .model(v.getModel())
                .year(v.getYear())
                .mileageKm(v.getMileageKm())
                .vehicleCategory(v.getVehicleCategory().name())
                .perKmRate(v.getPerKmRate())
                .approvalStatus(v.getApprovalStatus().name())
                .rejectionReason(v.getRejectionReason())
                .photoFrontUrl(v.getPhotoFrontUrl())
                .photoBackUrl(v.getPhotoBackUrl())
                .photoSideUrl(v.getPhotoSideUrl())
                .photoInteriorUrl(v.getPhotoInteriorUrl())
                .vehicleLicenceUrl(v.getVehicleLicenceUrl())
                .insuranceUrl(v.getInsuranceUrl())
                .insuranceExpiry(v.getInsuranceExpiry())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
