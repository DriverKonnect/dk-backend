package com.driverkonnect.backend.dto.response.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverApplicationResponseDto {
    private Long id;
    private String applicationNumber;
    private String status;
    private String fullName;
    private String email;
    private Boolean privacyPolicyAccepted;
    private String privacyPolicyVersion;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
