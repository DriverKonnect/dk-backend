package com.driverkonnect.backend.dto.response.tourcompany;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TourCompanyProfileResponseDto {

    private Long id;
    private String companyName;
    private String businessRegistrationNumber;
    private String contactPersonFirstName;
    private String contactPersonLastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
