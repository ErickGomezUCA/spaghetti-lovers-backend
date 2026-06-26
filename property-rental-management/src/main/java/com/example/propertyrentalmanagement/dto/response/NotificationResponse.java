package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Notification;
import com.example.propertyrentalmanagement.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        UUID reservationId,
        NotificationType type,
        String title,
        String message,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getReservation() != null
                        ? notification.getReservation().getId()
                        : null,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
