package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, UUID> {
    List<MaintenanceSchedule> findByPropertyId(UUID propertyId);
}
