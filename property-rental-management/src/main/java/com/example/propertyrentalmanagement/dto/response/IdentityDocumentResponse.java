package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.IdentityDocument;
import com.example.propertyrentalmanagement.enums.DocumentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record IdentityDocumentResponse(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        String userRole,
        String documentUrl,
        DocumentStatus documentStatus,
        LocalDateTime submittedAt,
        String reviewedBy,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
    public static IdentityDocumentResponse fromEntity(IdentityDocument document) {
        String reviewerName = document.getReviewedBy() != null ? document.getReviewedBy().getName() : null;

        return new IdentityDocumentResponse(
                document.getId(),
                document.getUser().getId(),
                document.getUser().getName(),
                document.getUser().getEmail(),
                document.getUser().getRole().name(),
                document.getDocumentUrl(),
                document.getDocumentStatus(),
                document.getCreatedAt(),
                reviewerName,
                document.getReviewedAt(),
                document.getRejectionReason()
        );
    }
}