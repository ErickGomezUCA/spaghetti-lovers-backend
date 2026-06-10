package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.AvailabilityCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityCalendarRepository extends JpaRepository<AvailabilityCalendar, UUID> {

    @Query("SELECT a FROM AvailabilityCalendar a WHERE a.property.id = :propertyId " +
            "AND a.timestampStart < :endDate " +
            "AND a.timestampEnd > :startDate")
    List<AvailabilityCalendar> findOverlappingBlocks(
            @Param("propertyId") UUID propertyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}