package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.admin.VehicleTypeRequestDto;
import com.driverkonnect.backend.dto.response.admin.VehicleTypeResponseDto;

import java.util.List;

public interface VehicleTypeService {
    VehicleTypeResponseDto create(VehicleTypeRequestDto dto);
    List<VehicleTypeResponseDto> getAll();
    VehicleTypeResponseDto getById(Long id);
    VehicleTypeResponseDto update(Long id, VehicleTypeRequestDto dto);
    VehicleTypeResponseDto toggleActive(Long id);
}
