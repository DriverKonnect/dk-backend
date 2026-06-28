package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.TourCompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TourCompanyProfileRepository extends JpaRepository<TourCompanyProfile, Long> {

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    Optional<TourCompanyProfile> findByUser_Email(String email);
}
