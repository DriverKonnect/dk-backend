package com.driverkonnect.backend.entity;

import com.driverkonnect.backend.enums.ApplicationStatus;
import com.driverkonnect.backend.enums.AvailabilityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "driver_applications")
public class DriverApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_number", unique = true)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "whatsapp")
    private String whatsapp;

    @Column(name = "nic_number", nullable = false, unique = true)
    private String nicNumber;

    @Column(name = "languages_spoken", nullable = false, columnDefinition = "TEXT")
    private String languagesSpoken;

    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false)
    private AvailabilityType availability;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Column(name = "privacy_policy_accepted", nullable = false)
    private Boolean privacyPolicyAccepted;

    @Column(name = "privacy_policy_accepted_at")
    private LocalDateTime privacyPolicyAcceptedAt;

    @Column(name = "privacy_policy_version")
    private String privacyPolicyVersion;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DriverDocument> documents;
}
