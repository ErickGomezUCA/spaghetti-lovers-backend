package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateReservationRequest;
import com.example.propertyrentalmanagement.dto.request.ExtendReservationRequest;
import com.example.propertyrentalmanagement.dto.response.*;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);

    ReservationDetailResponse getReservationById(UUID reservationId);

    ReservationCancellationResponse cancelReservation(UUID reservationId);

    ReservationCompletionResponse completeReservation(UUID reservationId);

    ReservationExtensionResponse extendReservation(UUID reservationId, ExtendReservationRequest request);

    Page<ReservationResponse> getMyReservations(int page, int pageSize, String sortBy, String sortOrder, ReservationStatus status);

    Page<ReservationResponse> getLandlordReservations(int page, int pageSize, String sortBy, String sortOrder, ReservationStatus status, String searchTerm);

    LandlordReservationSummaryResponse getLandlordReservationSummary();

    Page<ReservationResponse> getAllSystemReservations(int page, int pageSize, String sortBy, String sortOrder, ReservationStatus status, String searchTerm);

    ReservationCancellationPreviewResponse previewCancellation(UUID reservationId);
}
