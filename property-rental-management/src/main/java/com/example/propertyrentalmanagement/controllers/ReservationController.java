package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateReservationRequest;
import com.example.propertyrentalmanagement.dto.response.*;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import com.example.propertyrentalmanagement.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final AccessCodeService accessCodeService;
    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("@authorizationService.isTenant()")
    public ResponseEntity<GenericResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);

        return GenericResponse.builder()
                .message("Reservation created successfully")
                .data(response)
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<GenericResponse> getReservationById(@PathVariable UUID reservationId) {
        ReservationDetailResponse response = reservationService.getReservationById(reservationId);

        return GenericResponse.builder()
                .message("Reservation details retrieved successfully")
                .data(response)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/{reservationId}/access-code")
    ResponseEntity<GenericResponse> getAccessCodeByReservationId(@PathVariable UUID reservationId) {
        AccessCodeResponse accessCode = accessCodeService.getActiveAccessCodeByReservationId(reservationId);

        return GenericResponse.builder()
                .message("Access code found")
                .data(accessCode)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @DeleteMapping("/{reservationId}")
    ResponseEntity<GenericResponse> cancelReservation(@PathVariable UUID reservationId) {
        ReservationCancellationResponse cancellationResponse = reservationService.cancelReservation(reservationId);

        return GenericResponse.builder()
                .message("Reservation cancelled successfully")
                .data(cancellationResponse)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
    @PostMapping("/{reservationId}/complete")
    ResponseEntity<GenericResponse> completeReservation(@PathVariable UUID reservationId) {
        ReservationCompletionResponse completionResponse = reservationService.completeReservation(reservationId);

        return GenericResponse.builder()
                .message("Reservation completed successfully")
                .data(completionResponse)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }
}
