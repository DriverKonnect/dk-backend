package com.driverkonnect.backend.dto.request.driver;

import com.driverkonnect.backend.enums.AvailabilityType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverRegisterRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String whatsapp;

    @NotBlank(message = "NIC number is required")
    private String nicNumber;

    @NotEmpty(message = "At least one language must be selected")
    private List<String> languagesSpoken;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @NotNull(message = "Availability is required")
    private AvailabilityType availability;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean privacyPolicyAccepted;

    private String privacyPolicyVersion = "1.0";
}
