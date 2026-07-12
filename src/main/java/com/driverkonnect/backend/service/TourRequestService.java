package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.tourcompany.TourRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;

import java.util.List;

public interface TourRequestService {
    TourRequestResponseDto create(TourRequestDto dto);
    List<TourRequestSummaryDto> getMyTours();
    TourRequestResponseDto getById(Long id);
    TourRequestResponseDto update(Long id, TourRequestDto dto);
    TourRequestResponseDto publish(Long id);
}
