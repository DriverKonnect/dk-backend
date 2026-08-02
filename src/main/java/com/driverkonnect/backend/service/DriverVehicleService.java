package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.driver.DriverVehicleRequestDto;
import com.driverkonnect.backend.dto.response.driver.DriverVehicleResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverVehicleSummaryDto;
import com.driverkonnect.backend.enums.VehicleApprovalStatus;
import com.driverkonnect.backend.generics.PagedResponseDto;

import java.util.List;

public interface DriverVehicleService {

    DriverVehicleResponseDto addVehicle(DriverVehicleRequestDto dto);

    List<DriverVehicleResponseDto> getMyVehicles();

    DriverVehicleResponseDto getMyVehicleById(Long id);

    DriverVehicleResponseDto updateVehicle(Long id, DriverVehicleRequestDto dto);

    void removeVehicle(Long id);

    // Admin
    PagedResponseDto<AdminDriverVehicleSummaryDto> getAllVehicles(VehicleApprovalStatus status, int page, int size);

    AdminDriverVehicleSummaryDto approveVehicle(Long id);

    AdminDriverVehicleSummaryDto rejectVehicle(Long id, String rejectionReason);
}
