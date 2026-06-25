package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.DocumentStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewIdentityDocumentRequest(
        @NotNull(message = "Document status is required")
        DocumentStatus documentStatus
) {}