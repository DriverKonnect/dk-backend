package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.admin.TourApplicationStatusRequestDto;
import com.driverkonnect.backend.dto.response.admin.TourApplicationStatusResponseDto;

import java.util.List;

public interface TourApplicationStatusService {
    TourApplicationStatusResponseDto create(TourApplicationStatusRequestDto dto);
    List<TourApplicationStatusResponseDto> getAll();
    TourApplicationStatusResponseDto getById(Long id);
    TourApplicationStatusResponseDto update(Long id, TourApplicationStatusRequestDto dto);
    TourApplicationStatusResponseDto toggleActive(Long id);
    void delete(Long id);
}
