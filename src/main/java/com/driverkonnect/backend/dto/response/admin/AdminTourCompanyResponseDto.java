package com.driverkonnect.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminTourCompanyResponseDto {
    private Long id;
    private Long userId;
    private String companyName;
    private String businessRegistrationNumber;
    private String contactPersonFirstName;
    private String contactPersonLastName;
    private String email;
    private String phone;
    private String status;
    private Boolean isActive;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private String reviewedByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
