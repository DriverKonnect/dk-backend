package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.admin.CreateAdminUserRequestDto;
import com.driverkonnect.backend.dto.request.admin.UpdateAdminStatusRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminUserResponseDto;

import java.util.List;

public interface AdminUserService {
    List<AdminUserResponseDto> getAllAdminUsers();
    AdminUserResponseDto createAdminUser(CreateAdminUserRequestDto request);
    AdminUserResponseDto updateAdminStatus(Long userId, UpdateAdminStatusRequestDto request);
}
