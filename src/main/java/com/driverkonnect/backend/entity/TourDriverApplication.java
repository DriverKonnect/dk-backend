package com.driverkonnect.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tour_driver_applications")
public class TourDriverApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_request_id", nullable = false)
    private TourRequest tourRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_vehicle_id")
    private DriverVehicle driverVehicle;

    @Column(name = "per_km_rate_snapshot", precision = 10, scale = 2)
    private BigDecimal perKmRateSnapshot;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_application_status_id")
    private TourApplicationStatus tourApplicationStatus;

    @Column(name = "is_withdrawn", nullable = false)
    private Boolean isWithdrawn = false;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
