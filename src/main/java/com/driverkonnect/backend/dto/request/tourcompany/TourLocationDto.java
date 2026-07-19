package com.driverkonnect.backend.dto.request.tourcompany;

import com.driverkonnect.backend.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourLocationDto {

    @NotNull(message = "Location type is required")
    private LocationType locationType;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull(message = "Sequence order is required")
    private Integer sequenceOrder;
}
