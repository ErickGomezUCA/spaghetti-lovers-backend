package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        String propertyName,
        String propertyCity,
        String propertyDepartment,
        String propertyImage,
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

        String imageUrl = null;
        if (reservation.getProperty().getPhotos() != null && !reservation.getProperty().getPhotos().isEmpty()) {
            imageUrl = reservation.getProperty().getPhotos().get(0).getUrl();
        }

        return new ReservationResponse(
                reservation.getId(),
                reservation.getProperty().getTitle(),
                reservation.getProperty().getCity(),
                reservation.getProperty().getDepartment(),
                imageUrl,
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