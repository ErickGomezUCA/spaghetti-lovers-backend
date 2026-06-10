package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.ReservationCancellationResponse;

import java.util.UUID;

public interface ReservationService {

    ReservationCancellationResponse cancelReservation(UUID reservationId);
}
