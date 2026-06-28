package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Maintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
    Page<Maintenance> findAllByReportedById(UUID reportedById, Pageable pageable);

    Page<Maintenance> findAllByPropertyLandlordId(UUID landlordId, Pageable pageable);

    @Query("SELECT m FROM Maintenance m WHERE m.property.landlord.id = :landlordId " +
            "AND m.scheduledStart IS NOT NULL AND m.scheduledEnd IS NOT NULL " +
            "AND m.scheduledStart < :endDate AND m.scheduledEnd > :startDate")
    List<Maintenance> findByLandlordIdAndScheduledDateRange(
            @Param("landlordId") UUID landlordId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
