package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications(Boolean unreadOnly);

    long getUnreadCount();

    NotificationResponse markAsRead(UUID notificationId);

    void markAllAsRead();

    void deleteNotification(UUID notificationId);
}
