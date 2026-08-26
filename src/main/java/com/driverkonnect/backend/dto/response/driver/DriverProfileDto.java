package com.driverkonnect.backend.dto.response.driver;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DriverProfileDto {

    private String firstName;
    private String lastName;
    private String email;
    private Integer memberSinceYear;
    private Boolean isVerified;
    private Double rating;
    private long lifetimeTours;
    private Double onTimeRate;
    private LocalDate dateOfBirth;
    private String phone;
    private String whatsapp;
    private String nicNumber;
    private List<String> languagesSpoken;
    private Integer yearsOfExperience;
    private String availability;
    private List<DriverProfileDocumentDto> documents;
}
