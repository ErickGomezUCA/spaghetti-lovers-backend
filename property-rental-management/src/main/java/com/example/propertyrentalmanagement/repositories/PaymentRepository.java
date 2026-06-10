package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Payment;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByReservationAndPaymentType(
            Reservation reservation,
            PaymentType paymentType
    );
}
