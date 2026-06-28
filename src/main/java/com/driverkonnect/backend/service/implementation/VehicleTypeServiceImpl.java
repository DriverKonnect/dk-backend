package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.admin.VehicleTypeRequestDto;
import com.driverkonnect.backend.dto.response.admin.VehicleTypeResponseDto;
import com.driverkonnect.backend.entity.VehicleType;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.VehicleTypeRepository;
import com.driverkonnect.backend.service.VehicleTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleTypeServiceImpl implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;

    @Override
    public VehicleTypeResponseDto create(VehicleTypeRequestDto dto) {
        if (vehicleTypeRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new CustomException("Vehicle type with this name already exists", 409);
        }

        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(dto.getName().trim());
        vehicleType.setDescription(dto.getDescription());
        vehicleType.setIsActive(true);
        vehicleType.setCreatedAt(LocalDateTime.now());
        vehicleType.setUpdatedAt(LocalDateTime.now());

        vehicleType = vehicleTypeRepository.save(vehicleType);
        log.debug("Created vehicle type ID: {}, name: {}", vehicleType.getId(), vehicleType.getName());
        return toDto(vehicleType);
    }

    @Override
    public List<VehicleTypeResponseDto> getAll() {
        return vehicleTypeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public VehicleTypeResponseDto getById(Long id) {
        VehicleType vehicleType = findById(id);
        return toDto(vehicleType);
    }

    @Override
    public VehicleTypeResponseDto update(Long id, VehicleTypeRequestDto dto) {
        VehicleType vehicleType = findById(id);

        if (!vehicleType.getName().equalsIgnoreCase(dto.getName())
                && vehicleTypeRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new CustomException("Vehicle type with this name already exists", 409);
        }

        vehicleType.setName(dto.getName().trim());
        vehicleType.setDescription(dto.getDescription());
        vehicleType.setUpdatedAt(LocalDateTime.now());

        vehicleType = vehicleTypeRepository.save(vehicleType);
        log.debug("Updated vehicle type ID: {}", id);
        return toDto(vehicleType);
    }

    @Override
    public VehicleTypeResponseDto toggleActive(Long id) {
        VehicleType vehicleType = findById(id);
        vehicleType.setIsActive(!vehicleType.getIsActive());
        vehicleType.setUpdatedAt(LocalDateTime.now());
        vehicleType = vehicleTypeRepository.save(vehicleType);
        log.debug("Toggled vehicle type ID: {} to isActive={}", id, vehicleType.getIsActive());
        return toDto(vehicleType);
    }

    @Override
    public void delete(Long id) {
        VehicleType vehicleType = findById(id);
        vehicleTypeRepository.delete(vehicleType);
        log.debug("Deleted vehicle type ID: {}", id);
    }

    private VehicleType findById(Long id) {
        return vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Vehicle type not found", 404));
    }

    private VehicleTypeResponseDto toDto(VehicleType vehicleType) {
        VehicleTypeResponseDto dto = new VehicleTypeResponseDto();
        dto.setId(vehicleType.getId());
        dto.setName(vehicleType.getName());
        dto.setDescription(vehicleType.getDescription());
        dto.setIsActive(vehicleType.getIsActive());
        dto.setCreatedAt(vehicleType.getCreatedAt());
        dto.setUpdatedAt(vehicleType.getUpdatedAt());
        return dto;
    }
}
