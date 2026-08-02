package com.driverkonnect.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VehicleRejectionDto {

    @NotBlank
    @Size(max = 500)
    private String rejectionReason;
}
