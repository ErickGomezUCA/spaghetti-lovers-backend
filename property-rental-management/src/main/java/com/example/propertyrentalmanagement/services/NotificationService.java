package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    Page<NotificationResponse> getMyNotifications(
            Boolean unreadOnly,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder
    );

    long getUnreadCount();

    NotificationResponse markAsRead(UUID notificationId);

    void markAllAsRead();

    void deleteNotification(UUID notificationId);
}
