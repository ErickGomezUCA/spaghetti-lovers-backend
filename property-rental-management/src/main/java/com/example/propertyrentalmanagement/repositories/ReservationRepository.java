package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

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

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.tenant.id = :tenantId " +
            "AND r.reservationStatus IN (com.example.propertyrentalmanagement.enums.ReservationStatus.RESERVED, " +
            "com.example.propertyrentalmanagement.enums.ReservationStatus.ACTIVE) " +
            "AND (:checkIn < r.checkOutDate AND :checkOut > r.checkInDate)")
    boolean hasOverlappingReservations(@Param("tenantId") UUID tenantId,
                                       @Param("checkIn") LocalDate checkIn,
                                       @Param("checkOut") LocalDate checkOut);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.property.id = :propertyId " +
            "AND r.reservationStatus IN (com.example.propertyrentalmanagement.enums.ReservationStatus.RESERVED, " +
            "com.example.propertyrentalmanagement.enums.ReservationStatus.ACTIVE) " +
            "AND r.checkInDate <= :today AND r.checkOutDate >= :today")
    boolean isPropertyOccupiedToday(@Param("propertyId") UUID propertyId,
                                    @Param("today") LocalDate today);

    List<Reservation> findByCheckInDateAndReservationStatus(LocalDate checkInDate, ReservationStatus status);

    Long countByTenantId(UUID tenantId);

    Long countByTenantIdAndReservationStatus(UUID tenantId, ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.property.landlord.id = :landlordId")
    Long countByPropertyLandlordId(@Param("landlordId") UUID landlordId);

    Page<Reservation> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Reservation> findByTenantIdAndReservationStatus(UUID tenantId, ReservationStatus status, Pageable pageable);

    interface StatusCount {
        ReservationStatus getStatus();
        Long getCount();
    }

    @Query("SELECT r.reservationStatus AS status, COUNT(r) AS count " +
            "FROM Reservation r WHERE r.property.landlord.id = :landlordId " +
            "GROUP BY r.reservationStatus")
    List<StatusCount> countReservationsGroupedByStatus(@Param("landlordId") UUID landlordId);
    
    @Query("SELECT r FROM Reservation r WHERE r.property.landlord.id = :landlordId " +
            "AND (:status IS NULL OR r.reservationStatus = :status) " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(r.property.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.tenant.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.tenant.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Reservation> findLandlordReservationsWithFilters(
            @Param("landlordId") UUID landlordId,
            @Param("status") ReservationStatus status,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}
