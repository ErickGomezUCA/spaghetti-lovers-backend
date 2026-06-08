package com.example.propertyrentalmanagement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRatingRequest (
        @NotNull(message = "reservationId is required")
        UUID reservationId,

        @NotNull(message = "score is required")
        @Min(value = 1, message = "Minimum score is 1")
        @Max(value = 5, message = "Maximum score is 5")
        Integer score,

        String comment
) {
}
