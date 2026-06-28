package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.AvailabilityCalendar;
import com.example.propertyrentalmanagement.entitites.Maintenance;
import com.example.propertyrentalmanagement.entitites.Reservation;
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
  
    @Query("SELECT a FROM AvailabilityCalendar a WHERE a.property.landlord.id = :landlordId " +
            "AND a.timestampStart < :endDate AND a.timestampEnd > :startDate")
    List<AvailabilityCalendar> findByLandlordIdAndDateRange(
            @Param("landlordId") UUID landlordId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<AvailabilityCalendar> findByReservation(Reservation reservation);

    void deleteByMaintenance(Maintenance maintenance);

    @Query("SELECT ac FROM AvailabilityCalendar ac WHERE ac.property.id = :propertyId " +
            "AND ac.timestampStart < :newCheckOutTime " +
            "AND ac.timestampEnd > :currentCheckOutTime " +
            "AND (ac.reservation IS NULL OR ac.reservation.id != :reservationId)")
    List<AvailabilityCalendar> findExtensionOverlaps(
            @Param("propertyId") UUID propertyId,
            @Param("newCheckOutTime") LocalDateTime newCheckOutTime,
            @Param("currentCheckOutTime") LocalDateTime currentCheckOutTime,
            @Param("reservationId") UUID reservationId
    );
}
