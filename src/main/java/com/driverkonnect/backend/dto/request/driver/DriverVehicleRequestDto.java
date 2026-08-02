package com.driverkonnect.backend.dto.request.driver;

import com.driverkonnect.backend.enums.VehicleCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DriverVehicleRequestDto {

    @NotBlank
    @Size(max = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    private String model;

    @NotNull
    @Min(1990)
    private Integer year;

    @NotNull
    @Min(0)
    private Integer mileageKm;

    @NotNull
    private VehicleCategory vehicleCategory;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal perKmRate;

    private LocalDate insuranceExpiry;
}
