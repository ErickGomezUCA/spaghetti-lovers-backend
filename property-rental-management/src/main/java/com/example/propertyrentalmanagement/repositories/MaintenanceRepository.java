package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Maintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
    Page<Maintenance> findAllByReportedById(UUID reportedById, Pageable pageable);

    Page<Maintenance> findAllByPropertyLandlordId(UUID landlordId, Pageable pageable);
}
