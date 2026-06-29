package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewIdentityDocumentRequest(
        @NotNull(message = "Document status is required")
        DocumentStatus documentStatus,

        @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
        String rejectionReason
) {}