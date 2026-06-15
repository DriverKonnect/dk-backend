package com.driverkonnect.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
    private Boolean isFirstLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
