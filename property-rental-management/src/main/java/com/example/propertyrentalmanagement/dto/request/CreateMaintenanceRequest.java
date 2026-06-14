package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateMaintenanceRequest(
        @NotNull
        UUID reservationId,

        @NotBlank
        String title,

        String description,

        @NotNull
        Urgency urgency,

        List<String> photoUrls
) {
}
