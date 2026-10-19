package com.driverkonnect.backend.dto.request.driver;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateDriverProfileDto {

    @NotBlank
    @Size(max = 255)
    private String fullName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String whatsapp;

    @NotBlank
    @Size(max = 20)
    private String nicNumber;

    @NotEmpty
    private List<String> languagesSpoken;

    @NotNull
    @Min(0)
    private Integer yearsOfExperience;
}
