package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.FineType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FineRequest(
        @NotNull
        UUID reservationId,

        @NotNull
        FineType fineType,

        @NotBlank
        String description,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount
) {
}