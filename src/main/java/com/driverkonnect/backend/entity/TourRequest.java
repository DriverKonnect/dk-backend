package com.driverkonnect.backend.entity;

import com.driverkonnect.backend.enums.PaymentTerm;
import com.driverkonnect.backend.enums.TourStatus;
import com.driverkonnect.backend.enums.TravellerNationality;
import com.driverkonnect.backend.enums.TripType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tour_requests")
public class TourRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_company_id", nullable = false)
    private TourCompanyProfile tourCompany;

    @Column(name = "tour_name", nullable = false)
    private String tourName;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_type", nullable = false, length = 50)
    private TripType tripType;

    @Enumerated(EnumType.STRING)
    @Column(name = "traveller_nationality", nullable = false, length = 50)
    private TravellerNationality travellerNationality;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "days", nullable = false)
    private Integer days;

    @Column(name = "nights", nullable = false)
    private Integer nights;

    @Column(name = "pax_count", nullable = false)
    private Integer paxCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_type_id", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "estimated_km", precision = 10, scale = 2)
    private BigDecimal estimatedKm;

    @Column(name = "specific_requirements", columnDefinition = "TEXT")
    private String specificRequirements;

    @Column(name = "special_concerns", columnDefinition = "TEXT")
    private String specialConcerns;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_term", nullable = false, length = 50)
    private PaymentTerm paymentTerm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TourStatus status = TourStatus.DRAFT;

    @OneToMany(mappedBy = "tourRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    private List<TourLocation> locations;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
