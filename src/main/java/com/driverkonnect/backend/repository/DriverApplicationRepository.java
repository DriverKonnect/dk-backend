package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.DriverApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverApplicationRepository extends JpaRepository<DriverApplication, Long> {
    boolean existsByNicNumber(String nicNumber);
    Optional<DriverApplication> findByUser_Email(String email);
    List<DriverApplication> findAllByOrderByCreatedAtDesc();

    @Query("SELECT da FROM DriverApplication da LEFT JOIN FETCH da.documents WHERE da.id = :id")
    Optional<DriverApplication> findByIdWithDocuments(@Param("id") Long id);
}
