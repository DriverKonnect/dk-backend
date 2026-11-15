package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourCompanyProfile;
import com.driverkonnect.backend.enums.TourCompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TourCompanyProfileRepository extends JpaRepository<TourCompanyProfile, Long> {

    Optional<TourCompanyProfile> findByBusinessRegistrationNumber(String businessRegistrationNumber);

    Optional<TourCompanyProfile> findByUser_Email(String email);

    @EntityGraph(attributePaths = "user")
    Page<TourCompanyProfile> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<TourCompanyProfile> findByStatusOrderByCreatedAtDesc(TourCompanyStatus status, Pageable pageable);
}
