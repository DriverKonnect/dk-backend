package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.driver.DriverRegisterRequestDto;
import com.driverkonnect.backend.dto.response.driver.DriverApplicationResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface DriverRegistrationService {
    DriverApplicationResponseDto register(DriverRegisterRequestDto request);
    DriverApplicationResponseDto uploadDocuments(Long applicationId,
                                                  MultipartFile licenceFront,
                                                  MultipartFile licenceBack,
                                                  MultipartFile policeClearance);
    DriverApplicationResponseDto getMyApplication();
}
