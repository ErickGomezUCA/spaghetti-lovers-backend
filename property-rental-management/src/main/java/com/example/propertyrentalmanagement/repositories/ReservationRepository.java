package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByTenantIdAndReservationStatusIn(
            UUID tenantId,
            List<ReservationStatus> statuses
    );

    @Query("SELECT r FROM Reservation r WHERE r.property.id = :propertyId " +
            "AND r.reservationStatus != :cancelledStatus " +
            "AND r.checkInDate >= :startDate " +
            "AND r.checkOutDate <= :endDate")
    List<Reservation> findByPropertyIdAndStatusNotCancelledAndDateRange(
            @Param("propertyId") UUID propertyId,
            @Param("cancelledStatus") ReservationStatus cancelledStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
