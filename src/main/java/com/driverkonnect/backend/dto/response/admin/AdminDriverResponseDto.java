package com.driverkonnect.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDriverResponseDto {
    private Long id;
    private String applicationNumber;
    private String status;
    private String rejectionReason;

    private String fullName;
    private LocalDate dateOfBirth;
    private String phone;
    private String whatsapp;
    private String nicNumber;
    private List<String> languagesSpoken;
    private Integer yearsOfExperience;
    private String availability;

    private Boolean privacyPolicyAccepted;
    private String privacyPolicyVersion;
    private LocalDateTime privacyPolicyAcceptedAt;

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;

    private List<AdminDocumentDto> documents;

    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
