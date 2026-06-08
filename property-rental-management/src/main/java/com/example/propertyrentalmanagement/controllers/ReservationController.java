package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.response.AccessCodeResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final AccessCodeService accessCodeService;

    @GetMapping("/{reservationId}/access-code")
    ResponseEntity<GenericResponse> getAccessCodeByReservationId(@PathVariable UUID reservationId) {
        AccessCodeResponse accessCode = accessCodeService.getActiveAccessCodeByReservationId(reservationId);

        return GenericResponse.builder()
                .message("Access code found")
                .data(accessCode)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
