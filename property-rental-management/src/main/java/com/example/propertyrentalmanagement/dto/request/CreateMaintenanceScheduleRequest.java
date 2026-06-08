package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.MaintenanceScheduleFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMaintenanceScheduleRequest(
        @NotNull
        UUID propertyId,
        @NotBlank
        String title,
        String description,
        @NotNull
        MaintenanceScheduleFrequency frequency,
        @NotNull
        int interval,
        @NotBlank
        String nextScheduledDate // format: YYYY-MM-DDTHH:mm:ss
) {


}
