package com.example.propertyrentalmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitIdentityDocumentRequest(
        @NotBlank(message = "Document URL is required")
        String documentUrl
) {}