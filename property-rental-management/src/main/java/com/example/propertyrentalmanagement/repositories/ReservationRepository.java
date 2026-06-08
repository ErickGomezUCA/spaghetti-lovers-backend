package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByTenantIdAndReservationStatusIn(
            UUID tenantId,
            List<ReservationStatus> statuses
    );
}
