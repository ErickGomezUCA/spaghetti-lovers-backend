package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID propertyId,
        UUID tenantId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int guestsCount,
        int totalNights,
        BigDecimal baseTotal,
        BigDecimal cleaningFee,
        BigDecimal longStayDiscount,
        BigDecimal totalPrice,
        ReservationStatus reservationStatus,
        LocalDateTime createdAt
) {
    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getProperty().getId(),
                reservation.getTenant().getId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getGuestsCount(),
                reservation.getTotalNights(),
                reservation.getBaseTotal(),
                reservation.getCleaningFee(),
                reservation.getLongStayDiscount(),
                reservation.getTotalPrice(),
                reservation.getReservationStatus(),
                reservation.getCreatedAt()
        );
    }
}