package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.ReviewIdentityDocumentRequest;
import com.example.propertyrentalmanagement.dto.request.SubmitIdentityDocumentRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.IdentityDocumentResponse;
import com.example.propertyrentalmanagement.enums.DocumentStatus;
import com.example.propertyrentalmanagement.services.IdentityDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/identity-documents")
@RequiredArgsConstructor
public class IdentityDocumentController {

    private final IdentityDocumentService identityDocumentService;

    @PreAuthorize("@authorizationService.isAdmin()")
    @GetMapping
    public ResponseEntity<GenericResponse> getAllIdentityDocuments(
            @RequestParam(required = false) DocumentStatus status) {

        List<IdentityDocumentResponse> documents = identityDocumentService.getAllDocuments(status);

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message("Identity documents retrieved successfully.")
                        .data(documents)
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @PreAuthorize("@authorizationService.isLandlord() or @authorizationService.isTenant()")
    @PostMapping
    public ResponseEntity<GenericResponse> submitIdentityDocument(
            @Valid @RequestBody SubmitIdentityDocumentRequest request) {

        IdentityDocumentResponse response = identityDocumentService.submitDocument(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GenericResponse.builder()
                        .message("Identity document submitted successfully and is pending verification.")
                        .data(response)
                        .status(HttpStatus.CREATED)
                        .build()
        );
    }

    @PreAuthorize("@authorizationService.isAdmin()")
    @PatchMapping("/{documentId}")
    public ResponseEntity<GenericResponse> reviewIdentityDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody ReviewIdentityDocumentRequest request) {

        IdentityDocumentResponse response = identityDocumentService.reviewDocument(documentId, request);

        String dynamicMessage = request.documentStatus() == com.example.propertyrentalmanagement.enums.DocumentStatus.VERIFIED
                ? "Identity document has been successfully verified."
                : "Identity document has been rejected.";

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message(dynamicMessage)
                        .data(response)
                        .status(HttpStatus.OK)
                        .build()
        );
    }
}