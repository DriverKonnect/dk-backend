package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.admin.TourApplicationStatusRequestDto;
import com.driverkonnect.backend.dto.response.admin.TourApplicationStatusResponseDto;
import com.driverkonnect.backend.entity.TourApplicationStatus;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.TourApplicationStatusRepository;
import com.driverkonnect.backend.service.TourApplicationStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourApplicationStatusServiceImpl implements TourApplicationStatusService {

    private final TourApplicationStatusRepository tourApplicationStatusRepository;

    @Override
    public TourApplicationStatusResponseDto create(TourApplicationStatusRequestDto dto) {
        if (tourApplicationStatusRepository.existsByLabelIgnoreCase(dto.getLabel())) {
            throw new CustomException("Tour application status with this label already exists", 409);
        }

        TourApplicationStatus status = new TourApplicationStatus();
        status.setLabel(dto.getLabel().trim());
        status.setDescription(dto.getDescription());
        status.setIsActive(true);
        status.setCreatedAt(LocalDateTime.now());
        status.setUpdatedAt(LocalDateTime.now());

        status = tourApplicationStatusRepository.save(status);
        log.debug("Created tour application status ID: {}, label: {}", status.getId(), status.getLabel());
        return toDto(status);
    }

    @Override
    public List<TourApplicationStatusResponseDto> getAll() {
        return tourApplicationStatusRepository.findAllByOrderByLabelAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public TourApplicationStatusResponseDto getById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public TourApplicationStatusResponseDto update(Long id, TourApplicationStatusRequestDto dto) {
        TourApplicationStatus status = findById(id);

        if (!status.getLabel().equalsIgnoreCase(dto.getLabel())
                && tourApplicationStatusRepository.existsByLabelIgnoreCase(dto.getLabel())) {
            throw new CustomException("Tour application status with this label already exists", 409);
        }

        status.setLabel(dto.getLabel().trim());
        status.setDescription(dto.getDescription());
        status.setUpdatedAt(LocalDateTime.now());

        status = tourApplicationStatusRepository.save(status);
        log.debug("Updated tour application status ID: {}", id);
        return toDto(status);
    }

    @Override
    public TourApplicationStatusResponseDto toggleActive(Long id) {
        TourApplicationStatus status = findById(id);
        status.setIsActive(!status.getIsActive());
        status.setUpdatedAt(LocalDateTime.now());
        status = tourApplicationStatusRepository.save(status);
        log.debug("Toggled tour application status ID: {} to isActive={}", id, status.getIsActive());
        return toDto(status);
    }

    private TourApplicationStatus findById(Long id) {
        return tourApplicationStatusRepository.findById(id)
                .orElseThrow(() -> new CustomException("Tour application status not found", 404));
    }

    private TourApplicationStatusResponseDto toDto(TourApplicationStatus status) {
        TourApplicationStatusResponseDto dto = new TourApplicationStatusResponseDto();
        dto.setId(status.getId());
        dto.setLabel(status.getLabel());
        dto.setDescription(status.getDescription());
        dto.setIsActive(status.getIsActive());
        dto.setCreatedAt(status.getCreatedAt());
        dto.setUpdatedAt(status.getUpdatedAt());
        return dto;
    }
}
