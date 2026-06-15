package com.driverkonnect.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDriverSummaryDto {
    private Long id;
    private String applicationNumber;
    private String status;
    private String fullName;
    private String email;
    private String phone;
    private String nicNumber;
    private Boolean isActive;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
