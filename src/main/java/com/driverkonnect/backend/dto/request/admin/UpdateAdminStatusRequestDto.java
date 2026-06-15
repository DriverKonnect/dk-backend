package com.driverkonnect.backend.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAdminStatusRequestDto {

    @NotNull(message = "isActive status is required")
    private Boolean isActive;
}
