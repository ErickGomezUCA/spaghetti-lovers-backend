package com.example.propertyrentalmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmMaintenanceRequest(
        @NotBlank
        String scheduledStart, // Both fields have HTML format value (YYYY-MM-DDTHH:mm)
        @NotBlank
        String scheduledEnd,
        boolean blockCalendar
) {
}
