package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.IdentityDocument;
import com.example.propertyrentalmanagement.enums.DocumentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record IdentityDocumentResponse(
        UUID id,
        UUID userId,
        String documentUrl,
        DocumentStatus documentStatus
) {
    public static IdentityDocumentResponse fromEntity(IdentityDocument document) {
        return new IdentityDocumentResponse(
                document.getId(),
                document.getUser().getId(),
                document.getDocumentUrl(),
                document.getDocumentStatus()
        );
    }
}