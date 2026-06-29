package com.example.propertyrentalmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentIntentRequest(
        @NotNull(message = "paymentId is required")
        UUID paymentId
) {}
