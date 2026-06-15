package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.admin.AdminRejectRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminDriverSummaryDto;

import java.util.List;

public interface AdminService {
    List<AdminDriverSummaryDto> getAllApplications();
    AdminDriverResponseDto getApplication(Long applicationId);
    AdminDriverResponseDto approveApplication(Long applicationId);
    AdminDriverResponseDto rejectApplication(Long applicationId, AdminRejectRequestDto request);
    AdminDriverResponseDto deactivateDriver(Long applicationId);
}
