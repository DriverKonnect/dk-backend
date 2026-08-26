package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.driver.UpdateDriverProfileDto;
import com.driverkonnect.backend.dto.response.driver.DriverProfileDto;

public interface DriverProfileService {
    DriverProfileDto getProfile();
    DriverProfileDto updateProfile(UpdateDriverProfileDto dto);
}
