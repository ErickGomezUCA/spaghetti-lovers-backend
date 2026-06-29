package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.NotificationResponse;
import com.example.propertyrentalmanagement.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.propertyrentalmanagement.dto.response.PaginationMeta;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<GenericResponse> getMyNotifications(
            @RequestParam(defaultValue = "false") Boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<NotificationResponse> notifications =
                notificationService.getMyNotifications(
                        unreadOnly,
                        page,
                        pageSize,
                        sortBy,
                        sortOrder
                );

        return GenericResponse.builder()
                .message("Notifications retrieved successfully")
                .data(notifications.getContent())
                .pagination(PaginationMeta.fromPage(notifications))
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<GenericResponse> getUnreadCount() {
        long unreadCount = notificationService.getUnreadCount();

        return GenericResponse.builder()
                .message("Unread notifications count retrieved successfully")
                .data(unreadCount)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<GenericResponse> markAsRead(
            @PathVariable UUID notificationId
    ) {
        NotificationResponse response =
                notificationService.markAsRead(notificationId);

        return GenericResponse.builder()
                .message("Notification marked as read successfully")
                .data(response)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<GenericResponse> markAllAsRead() {
        notificationService.markAllAsRead();

        return GenericResponse.builder()
                .message("All notifications marked as read successfully")
                .data(null)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<GenericResponse> deleteNotification(
            @PathVariable UUID notificationId
    ) {
        notificationService.deleteNotification(notificationId);

        return GenericResponse.builder()
                .message("Notification deleted successfully")
                .data(null)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }
}
