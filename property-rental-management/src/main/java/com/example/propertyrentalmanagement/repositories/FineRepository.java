package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Fine;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.FineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FineRepository extends JpaRepository<Fine, UUID> {

    List<Fine> findByReservationAndFineTypeAndResolvedAtIsNull(
            Reservation reservation,
            FineType fineType
    );

    List<Fine> findByReservationAndResolvedAtIsNull(Reservation reservation);

    @Query("SELECT f FROM Fine f WHERE f.reservation.property.landlord.id = :landlordId " +
            "AND (:fineType IS NULL OR f.fineType = :fineType) " +
            "AND (:resolved IS NULL " +
            "     OR (:resolved = true AND f.resolvedAt IS NOT NULL) " +
            "     OR (:resolved = false AND f.resolvedAt IS NULL)) " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "     LOWER(CAST(f.id AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(f.reservation.property.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(f.reservation.tenant.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Fine> findLandlordFinesWithFilters(
            @Param("landlordId") UUID landlordId,
            @Param("fineType") FineType fineType,
            @Param("resolved") Boolean resolved,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    long countByReservation_Property_LandlordId(UUID landlordId);

    long countByReservation_Property_LandlordIdAndResolvedAtIsNull(UUID landlordId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f " +
            "WHERE f.reservation.property.landlord.id = :landlordId AND f.resolvedAt IS NULL")
    BigDecimal sumPendingAmountByLandlord(@Param("landlordId") UUID landlordId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f " +
            "WHERE f.reservation.property.landlord.id = :landlordId AND f.resolvedAt IS NOT NULL")
    BigDecimal sumResolvedAmountByLandlord(@Param("landlordId") UUID landlordId);

    Page<Fine> findByReservation_Tenant_Id(UUID tenantId, Pageable pageable);
}
