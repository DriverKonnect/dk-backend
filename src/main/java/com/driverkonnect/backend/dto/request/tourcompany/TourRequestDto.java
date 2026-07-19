package com.driverkonnect.backend.dto.request.tourcompany;

import com.driverkonnect.backend.enums.PaymentTerm;
import com.driverkonnect.backend.enums.TravellerNationality;
import com.driverkonnect.backend.enums.TripType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourRequestDto {

    @NotBlank(message = "Tour name is required")
    @Size(max = 255, message = "Tour name must not exceed 255 characters")
    private String tourName;

    @NotNull(message = "Trip type is required")
    private TripType tripType;

    @NotNull(message = "Traveller nationality is required")
    private TravellerNationality travellerNationality;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Days is required")
    @Min(value = 1, message = "Days must be at least 1")
    private Integer days;

    @NotNull(message = "Nights is required")
    @Min(value = 0, message = "Nights must be 0 or more")
    private Integer nights;

    @NotNull(message = "Pax count is required")
    @Min(value = 1, message = "Pax count must be at least 1")
    private Integer paxCount;

    @NotNull(message = "Vehicle type is required")
    private Long vehicleTypeId;

    @DecimalMin(value = "0.0", message = "Estimated KM must be a positive value")
    private BigDecimal estimatedKm;

    private String specificRequirements;
    private String specialConcerns;

    @NotNull(message = "Payment term is required")
    private PaymentTerm paymentTerm;

    @NotEmpty(message = "At least one location is required")
    @Valid
    private List<TourLocationDto> locations;
}
