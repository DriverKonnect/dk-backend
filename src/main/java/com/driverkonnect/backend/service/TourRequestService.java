package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.tourcompany.TourRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestResponseDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourRequestSummaryDto;
import com.driverkonnect.backend.enums.TourStatus;
import com.driverkonnect.backend.generics.PagedResponseDto;

import java.time.LocalDate;

public interface TourRequestService {
    TourRequestResponseDto create(TourRequestDto dto);
    PagedResponseDto<TourRequestSummaryDto> getMyTours(TourStatus status, LocalDate dateFrom, LocalDate dateTo, int page, int size);
    TourRequestResponseDto getById(Long id);
    TourRequestResponseDto update(Long id, TourRequestDto dto);
    TourRequestResponseDto publish(Long id);
}
