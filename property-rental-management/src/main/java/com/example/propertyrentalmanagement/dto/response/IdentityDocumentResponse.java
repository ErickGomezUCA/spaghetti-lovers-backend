package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.IdentityDocument;
import java.time.LocalDateTime;
import java.util.UUID;

public record IdentityDocumentResponse(
        UUID id,
        UUID userId,
        String documentUrl,
        String documentStatus
) {
    public static IdentityDocumentResponse fromEntity(IdentityDocument document) {
        return new IdentityDocumentResponse(
                document.getId(),
                document.getUser().getId(),
                document.getDocumentUrl(),
                document.getDocumentStatus().name()
        );
    }
}