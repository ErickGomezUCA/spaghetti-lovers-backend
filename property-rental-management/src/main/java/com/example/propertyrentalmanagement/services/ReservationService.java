package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateReservationRequest;
import com.example.propertyrentalmanagement.dto.request.ExtendReservationRequest;
import com.example.propertyrentalmanagement.dto.response.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);

    ReservationDetailResponse getReservationById(UUID reservationId);

    ReservationCancellationResponse cancelReservation(UUID reservationId);

    ReservationCompletionResponse completeReservation(UUID reservationId);

    ReservationExtensionResponse extendReservation(UUID reservationId, ExtendReservationRequest request);
}
