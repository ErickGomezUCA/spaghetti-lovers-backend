package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.FineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.services.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @PostMapping
    @PreAuthorize("@authorizationService.isLandlord() or @authorizationService.isAdmin()")
    public ResponseEntity<GenericResponse> createFine(
            @Valid @RequestBody FineRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        FineResponse response = fineService.createFine(request, username, isAdmin);

        return GenericResponse.builder()
                .message("Fine created successfully")
                .data(response)
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }
}