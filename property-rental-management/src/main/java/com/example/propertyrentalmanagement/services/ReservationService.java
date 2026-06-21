package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateReservationRequest;
import com.example.propertyrentalmanagement.dto.response.ReservationCancellationResponse;
import com.example.propertyrentalmanagement.dto.response.ReservationCompletionResponse;
import com.example.propertyrentalmanagement.dto.response.ReservationDetailResponse;
import com.example.propertyrentalmanagement.dto.response.ReservationResponse;

import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);

    ReservationDetailResponse getReservationById(UUID reservationId);

    ReservationCancellationResponse cancelReservation(UUID reservationId);

    ReservationCompletionResponse completeReservation(UUID reservationId);
}
