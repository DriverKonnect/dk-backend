package com.driverkonnect.backend.service;

import com.driverkonnect.backend.dto.request.admin.AdminRejectRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminTourCompanyResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminTourCompanySummaryDto;
import com.driverkonnect.backend.enums.TourCompanyStatus;
import com.driverkonnect.backend.generics.PagedResponseDto;

public interface AdminTourCompanyService {

    PagedResponseDto<AdminTourCompanySummaryDto> getAllCompanies(TourCompanyStatus status, int page, int size);

    AdminTourCompanyResponseDto getCompany(Long companyId);

    AdminTourCompanyResponseDto approveCompany(Long companyId);

    AdminTourCompanyResponseDto rejectCompany(Long companyId, AdminRejectRequestDto request);

    AdminTourCompanyResponseDto deactivateCompany(Long companyId);

    AdminTourCompanyResponseDto reactivateCompany(Long companyId);
}
