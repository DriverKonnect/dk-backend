package com.driverkonnect.backend.controller;

import com.driverkonnect.backend.dto.request.admin.AdminRejectRequestDto;
import com.driverkonnect.backend.dto.response.admin.AdminTourCompanyResponseDto;
import com.driverkonnect.backend.dto.response.admin.AdminTourCompanySummaryDto;
import com.driverkonnect.backend.enums.TourCompanyStatus;
import com.driverkonnect.backend.generics.PagedResponseDto;
import com.driverkonnect.backend.generics.Response;
import com.driverkonnect.backend.service.AdminTourCompanyService;
import com.driverkonnect.backend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/tour-companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Tour Companies", description = "Review tour company registrations and approve, reject, deactivate, or reactivate tour company accounts.")
public class AdminTourCompanyController {

    private final AdminTourCompanyService adminTourCompanyService;

    @GetMapping
    @Operation(
            summary = "List tour company registrations",
            description = "Returns a paginated list of tour companies, newest first. Filter by status (PENDING, APPROVED, REJECTED, DEACTIVATED) to focus on registrations awaiting review."
    )
    public ResponseEntity<Response<PagedResponseDto<AdminTourCompanySummaryDto>>> getAllCompanies(
            @Parameter(description = "Filter by status") @RequestParam(required = false) TourCompanyStatus status,
            @Parameter(description = "Page number (0-indexed, default 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10)") @RequestParam(defaultValue = "10") int size) {
        log.info("Admin requesting tour companies - status={}, page={}, size={}", status, page, size);
        PagedResponseDto<AdminTourCompanySummaryDto> result = adminTourCompanyService.getAllCompanies(status, page, size);
        log.info("Successfully retrieved {} tour companies", result.getContent().size());
        return ResponseUtil.success(result, "Tour companies retrieved successfully");
    }

    @GetMapping("/{companyId}")
    @Operation(
            summary = "Get a tour company by ID",
            description = "Retrieves the full details of a tour company registration, including review information. Returns 404 if not found."
    )
    public ResponseEntity<Response<AdminTourCompanyResponseDto>> getCompany(
            @Parameter(description = "Tour company profile ID") @PathVariable Long companyId) {
        log.info("Received request to retrieve tour company ID: {}", companyId);
        AdminTourCompanyResponseDto result = adminTourCompanyService.getCompany(companyId);
        return ResponseUtil.success(result, "Tour company retrieved successfully");
    }

    @PutMapping("/{companyId}/approve")
    @Operation(
            summary = "Approve a tour company registration",
            description = "Approves a PENDING or REJECTED registration and activates the company's account so it can log in. Returns 400 if already approved or deactivated, 404 if not found."
    )
    public ResponseEntity<Response<AdminTourCompanyResponseDto>> approveCompany(
            @Parameter(description = "Tour company profile ID") @PathVariable Long companyId) {
        log.info("Received request to approve tour company ID: {}", companyId);
        AdminTourCompanyResponseDto result = adminTourCompanyService.approveCompany(companyId);
        log.info("Successfully approved tour company ID: {}", companyId);
        return ResponseUtil.success(result, "Tour company approved successfully");
    }

    @PutMapping("/{companyId}/reject")
    @Operation(
            summary = "Reject a tour company registration",
            description = "Rejects a PENDING registration. An optional reason can be supplied and is shown to the company when it tries to log in. The company may resubmit afterwards. Returns 400 if not pending, 404 if not found."
    )
    public ResponseEntity<Response<AdminTourCompanyResponseDto>> rejectCompany(
            @Parameter(description = "Tour company profile ID") @PathVariable Long companyId,
            @RequestBody(required = false) AdminRejectRequestDto request) {
        log.info("Received request to reject tour company ID: {}", companyId);
        AdminTourCompanyResponseDto result = adminTourCompanyService.rejectCompany(
                companyId, request != null ? request : new AdminRejectRequestDto());
        log.info("Successfully rejected tour company ID: {}", companyId);
        return ResponseUtil.success(result, "Tour company registration rejected");
    }

    @PutMapping("/{companyId}/deactivate")
    @Operation(
            summary = "Deactivate a tour company",
            description = "Deactivates an APPROVED tour company, disabling login and revoking its refresh tokens. Returns 400 if not approved, 404 if not found."
    )
    public ResponseEntity<Response<AdminTourCompanyResponseDto>> deactivateCompany(
            @Parameter(description = "Tour company profile ID") @PathVariable Long companyId) {
        log.info("Received request to deactivate tour company ID: {}", companyId);
        AdminTourCompanyResponseDto result = adminTourCompanyService.deactivateCompany(companyId);
        log.info("Successfully deactivated tour company ID: {}", companyId);
        return ResponseUtil.success(result, "Tour company deactivated");
    }

    @PutMapping("/{companyId}/reactivate")
    @Operation(
            summary = "Reactivate a tour company",
            description = "Reactivates a DEACTIVATED tour company, restoring its APPROVED status and login access. Returns 400 if not deactivated, 404 if not found."
    )
    public ResponseEntity<Response<AdminTourCompanyResponseDto>> reactivateCompany(
            @Parameter(description = "Tour company profile ID") @PathVariable Long companyId) {
        log.info("Received request to reactivate tour company ID: {}", companyId);
        AdminTourCompanyResponseDto result = adminTourCompanyService.reactivateCompany(companyId);
        log.info("Successfully reactivated tour company ID: {}", companyId);
        return ResponseUtil.success(result, "Tour company reactivated");
    }
}
