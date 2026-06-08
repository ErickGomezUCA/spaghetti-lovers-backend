package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.AccessCode;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.CodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
