package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateReservationRequest;
import com.example.propertyrentalmanagement.dto.request.ExtendReservationRequest;
import com.example.propertyrentalmanagement.dto.response.*;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import com.example.propertyrentalmanagement.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @PreAuthorize("@authorizationService.isTenant() or @authorizationService.isLandlord()")
    ResponseEntity<GenericResponse> getAccessCodeByReservationId(@PathVariable UUID reservationId) {
        AccessCodeResponse accessCode = accessCodeService.getActiveAccessCodeByReservationId(reservationId);

        return GenericResponse.builder()
                .message("Access code found")
                .data(accessCode)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @DeleteMapping("/{reservationId}")
    @PreAuthorize("@authorizationService.isTenant() or @authorizationService.isLandlord() or @authorizationService.isAdmin()")
    ResponseEntity<GenericResponse> cancelReservation(@PathVariable UUID reservationId) {
        ReservationCancellationResponse cancellationResponse = reservationService.cancelReservation(reservationId);

        return GenericResponse.builder()
                .message("Reservation cancelled successfully")
                .data(cancellationResponse)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
    @PostMapping("/{reservationId}/complete")
    @PreAuthorize("@authorizationService.isLandlord() or @authorizationService.isAdmin()")
    ResponseEntity<GenericResponse> completeReservation(@PathVariable UUID reservationId) {
        ReservationCompletionResponse completionResponse = reservationService.completeReservation(reservationId);

        return GenericResponse.builder()
                .message("Reservation completed successfully")
                .data(completionResponse)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }

    @PreAuthorize("@authorizationService.isTenant()")
    @PostMapping("/{reservationId}/extend")
    public ResponseEntity<GenericResponse> extendReservation(
            @PathVariable UUID reservationId,
            @Valid @RequestBody ExtendReservationRequest request) {

        ReservationExtensionResponse response = reservationService.extendReservation(reservationId, request);

        return GenericResponse.builder()
                .message("Reservation extended successfully")
                .data(response)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isTenant()")
    @GetMapping("/my-reservations")
    public ResponseEntity<GenericResponse> getMyReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) ReservationStatus status
    ) {

        Page<ReservationResponse> reservations = reservationService.getMyReservations(page, pageSize, sortBy, sortOrder, status);

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message("My reservations retrieved successfully")
                        .data(reservations.getContent())
                        .pagination(PaginationMeta.fromPage(reservations))
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @GetMapping("/landlord")
    public ResponseEntity<GenericResponse> getLandlordReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String search
    ) {
        Page<ReservationResponse> reservations = reservationService.getLandlordReservations(page, pageSize, sortBy, sortOrder, status, search);

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message("Landlord reservations retrieved successfully")
                        .data(reservations.getContent())
                        .pagination(PaginationMeta.fromPage(reservations))
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @GetMapping("/landlord/summary")
    public ResponseEntity<GenericResponse> getLandlordReservationSummary() {
        LandlordReservationSummaryResponse summary = reservationService.getLandlordReservationSummary();

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message("Landlord reservation summary retrieved successfully")
                        .data(summary)
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @GetMapping("/{reservationId}/cancellation-preview")
    @PreAuthorize("@authorizationService.isTenant() or @authorizationService.isLandlord() or @authorizationService.isAdmin()")
    public ResponseEntity<GenericResponse> previewCancellation(@PathVariable UUID reservationId) {
        ReservationCancellationPreviewResponse preview = reservationService.previewCancellation(reservationId);

        return GenericResponse.builder()
                .message("Reservation cancellation preview retrieved successfully")
                .data(preview)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }
}
