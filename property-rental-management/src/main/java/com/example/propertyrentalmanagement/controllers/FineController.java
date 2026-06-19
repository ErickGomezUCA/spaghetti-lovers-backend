package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateFineRequest;
import com.example.propertyrentalmanagement.dto.request.PayFineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.services.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @PostMapping
    @PreAuthorize("@authorizationService.isLandlord() or @authorizationService.isAdmin()")
    public ResponseEntity<GenericResponse> createFine(@Valid @RequestBody CreateFineRequest request) {

        FineResponse response = fineService.createFine(request);

        return GenericResponse.builder()
                .message("Fine created successfully")
                .data(response)
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @PostMapping("/{fineId}/pay")
    @PreAuthorize("@authorizationService.isTenant()")
    public ResponseEntity<GenericResponse> payFine(
            @PathVariable UUID fineId,
            @Valid @RequestBody PayFineRequest request) {

        FineResponse response = fineService.payFine(fineId, request);

        return GenericResponse.builder()
                .message("Fine paid successfully")
                .data(response)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}