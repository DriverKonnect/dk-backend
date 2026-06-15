package com.driverkonnect.backend.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRejectRequestDto {
    private String reason;
}
