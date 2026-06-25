package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        String propertyName,
        String tenantName,
        String tenantEmail,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int totalNights,
        int guestsCount,
        BigDecimal totalPrice,
        ReservationStatus reservationStatus
) {
    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getProperty().getTitle(),
                reservation.getTenant().getName(),
                reservation.getTenant().getEmail(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getTotalNights(),
                reservation.getGuestsCount(),
                reservation.getTotalPrice(),
                reservation.getReservationStatus()
        );
    }
}