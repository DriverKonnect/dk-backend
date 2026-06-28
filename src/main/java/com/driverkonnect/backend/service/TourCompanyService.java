package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.tourcompany.TourCompanyRegisterRequestDto;
import com.driverkonnect.backend.dto.response.tourcompany.TourCompanyProfileResponseDto;

public interface TourCompanyService {

    TourCompanyProfileResponseDto register(TourCompanyRegisterRequestDto request);

    TourCompanyProfileResponseDto getMyProfile();
}
