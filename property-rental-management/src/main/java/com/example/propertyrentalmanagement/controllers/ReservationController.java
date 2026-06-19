package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.response.AccessCodeResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.ReservationCancellationResponse;
import com.example.propertyrentalmanagement.dto.response.ReservationCompletionResponse;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import com.example.propertyrentalmanagement.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final AccessCodeService accessCodeService;
    private final ReservationService reservationService;

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
