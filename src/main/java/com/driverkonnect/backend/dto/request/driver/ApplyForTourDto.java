package com.driverkonnect.backend.dto.request.driver;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyForTourDto {

    @NotNull
    private Long vehicleId;

    private String note;
}
