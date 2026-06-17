package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Fine;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.FineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FineRepository extends JpaRepository<Fine, UUID> {

    List<Fine> findByReservationAndFineTypeAndResolvedAtIsNull(
            Reservation reservation,
            FineType fineType
    );

    List<Fine> findByReservationAndResolvedAtIsNull(Reservation reservation);
}
