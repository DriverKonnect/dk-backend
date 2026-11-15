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
public class AdminTourCompanySummaryDto {
    private Long id;
    private String companyName;
    private String businessRegistrationNumber;
    private String contactPersonFirstName;
    private String contactPersonLastName;
    private String email;
    private String phone;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
