package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Payment;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByReservationAndPaymentType(
            Reservation reservation,
            PaymentType paymentType
    );

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.reservation.id IN :reservationIds " +
            "AND p.paymentType IN :paymentTypes")
    BigDecimal sumAmountByReservationIdsAndPaymentTypes(
            @Param("reservationIds") List<UUID> reservationIds,
            @Param("paymentTypes") List<PaymentType> paymentTypes
    );

    List<Payment> findByReservation(Reservation reservation);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.paymentType IN :types AND p.createdAt BETWEEN :start AND :end")
    BigDecimal sumAmountByPaymentTypeInAndCreatedAtBetween(
            @Param("types") List<PaymentType> types,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
