package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.MaintenanceSchedule;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleFrequency;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleStatus;

import java.util.UUID;

public record MaintenanceScheduleResponse(
        UUID id,
        UUID propertyId,
        UUID scheduledBy,
        String title,
        String description,
        MaintenanceScheduleFrequency frequency,
        int interval,
        String lastCompletedAt, // format: YYYY-MM-DD
        String nextScheduledDate, // format: YYYY-MM-DD
        MaintenanceScheduleStatus status
) {
    public static MaintenanceScheduleResponse fromEntity(MaintenanceSchedule maintenanceSchedule) {
        return new MaintenanceScheduleResponse(
                maintenanceSchedule.getId(),
                maintenanceSchedule.getProperty() != null ? maintenanceSchedule.getProperty().getId() : null,
                maintenanceSchedule.getScheduledBy() != null ? maintenanceSchedule.getScheduledBy().getId() : null,
                maintenanceSchedule.getTitle(),
                maintenanceSchedule.getDescription(),
                maintenanceSchedule.getFrequency(),
                maintenanceSchedule.getInterval(),
                maintenanceSchedule.getLastCompletedAt() != null ? maintenanceSchedule.getLastCompletedAt().toLocalDate().toString() : null,
                maintenanceSchedule.getNextScheduledDate() != null ? maintenanceSchedule.getNextScheduledDate().toLocalDate().toString() : null,
                maintenanceSchedule.getStatus()
        );
    }
}
