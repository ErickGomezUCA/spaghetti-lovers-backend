package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Maintenance;
import com.example.propertyrentalmanagement.entitites.MaintenancePhoto;
import com.example.propertyrentalmanagement.enums.MaintenanceStatus;
import com.example.propertyrentalmanagement.enums.Urgency;

import java.util.List;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id,
        UUID propertyId,
        UUID reservationId,
        UUID reportedId,
        String title,
        String description,
        Urgency urgency,
        String resolutionNotes,
        MaintenanceStatus maintenanceStatus,
        List<String> photoUrls
) {
    public static MaintenanceResponse fromEntity(Maintenance maintenance) {
        return new MaintenanceResponse(
                maintenance.getId(),
                maintenance.getProperty() != null ? maintenance.getProperty().getId() : null,
                maintenance.getReservation() != null ? maintenance.getReservation().getId() : null,
                maintenance.getReportedBy() != null ? maintenance.getReportedBy().getId() : null,
                maintenance.getTitle(),
                maintenance.getDescription(),
                maintenance.getUrgency(),
                maintenance.getResolutionNotes(),
                maintenance.getMaintenanceStatus(),
                maintenance.getPhotos() != null ? maintenance.getPhotos().stream().map(MaintenancePhoto::getUrl).toList() : List.of()
        );
    }
}
