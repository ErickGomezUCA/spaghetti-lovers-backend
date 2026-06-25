package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.MaintenanceSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, UUID> {
    Page<MaintenanceSchedule> findAllByPropertyId(UUID propertyId, Pageable pageable);
}
