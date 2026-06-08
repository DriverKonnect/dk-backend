package com.driverkonnect.backend.repository;

import com.driverkonnect.backend.entity.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
}
