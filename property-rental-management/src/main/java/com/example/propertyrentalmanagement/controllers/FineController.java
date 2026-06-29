package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateFineRequest;
import com.example.propertyrentalmanagement.dto.request.PayFineRequest;
import com.example.propertyrentalmanagement.dto.response.*;
import com.example.propertyrentalmanagement.enums.FineType;
import com.example.propertyrentalmanagement.services.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PreAuthorize("@authorizationService.isLandlord()")
    @GetMapping("/landlord/all")
    public ResponseEntity<GenericResponse> getLandlordFines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(defaultValue = "issuedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) FineType fineType,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) String search
    ) {
        Page<FineSummaryResponse> fines = fineService.getLandlordFines(page, pageSize, sortBy, sortOrder, fineType, resolved, search);

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message("Landlord fines retrieved successfully")
                        .data(fines.getContent())
                        .pagination(PaginationMeta.fromPage(fines))
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @GetMapping("/landlord/summary")
    public ResponseEntity<GenericResponse> getLandlordFinesSummary() {
        FineSummaryStatsResponse summary = fineService.getLandlordFinesSummary();

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message("Landlord fines summary retrieved successfully")
                        .data(summary)
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @PreAuthorize("@authorizationService.isTenant()")
    @GetMapping("/my-fines")
    public ResponseEntity<GenericResponse> getMyFines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int pageSize,
            @RequestParam(defaultValue = "issuedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<FineSummaryResponse> fines = fineService.getMyFines(page, pageSize, sortBy, sortOrder);

        return GenericResponse.builder()
                .message("My fines retrieved successfully")
                .data(fines.getContent())
                .pagination(PaginationMeta.fromPage(fines))
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}