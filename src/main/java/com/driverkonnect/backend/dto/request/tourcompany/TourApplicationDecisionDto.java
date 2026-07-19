package com.driverkonnect.backend.dto.request.tourcompany;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourApplicationDecisionDto {

    @NotNull(message = "Status ID is required")
    private Long statusId;
}
