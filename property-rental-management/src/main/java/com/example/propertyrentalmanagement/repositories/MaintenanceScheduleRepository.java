package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.MaintenanceSchedule;
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
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, UUID> {
    Page<MaintenanceSchedule> findAllByPropertyId(UUID propertyId, Pageable pageable);

    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE ms.property.landlord.id = :landlordId " +
            "AND ms.nextScheduledDate >= :startDate AND ms.nextScheduledDate < :endDate " +
            "AND ms.status <> 'DONE'")
    List<MaintenanceSchedule> findByLandlordIdAndNextScheduledDateBetween(
            @Param("landlordId") UUID landlordId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
