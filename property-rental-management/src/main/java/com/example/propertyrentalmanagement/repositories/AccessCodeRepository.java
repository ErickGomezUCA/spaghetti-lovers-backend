package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.AccessCode;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.CodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessCodeRepository extends JpaRepository<AccessCode, UUID> {

    Optional<AccessCode> findByReservationAndIsActiveTrueAndCodeType(
            Reservation reservation,
            CodeType codeType
    );

    List<AccessCode> findByReservation(Reservation reservation);
    List<AccessCode> findByReservation_Tenant_IdOrderByValidFromDesc(UUID tenantId);
    List<AccessCode> findByReservation_Property_Landlord_IdOrderByValidFromDesc(UUID landlordId);

    Page<AccessCode> findByReservation_Tenant_IdAndCodeType(
            UUID tenantId,
            CodeType codeType,
            Pageable pageable
    );

    Page<AccessCode> findByReservation_Property_Landlord_IdAndCodeType(
            UUID landlordId,
            CodeType codeType,
            Pageable pageable
    );
}
