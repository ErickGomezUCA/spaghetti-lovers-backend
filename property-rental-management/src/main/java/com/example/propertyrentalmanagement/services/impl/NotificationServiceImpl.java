package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.response.NotificationResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Notification;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.repositories.NotificationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Boolean unreadOnly) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        List<Notification> notifications = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId())
                : notificationRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.getId());

        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        return notificationRepository.countByUser_IdAndIsReadFalse(
                currentUser.getId()
        );
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        validateNotificationOwner(notification, currentUser);

        notification.setIsRead(true);

        return NotificationResponse.fromEntity(
                notificationRepository.save(notification)
        );
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        List<Notification> notifications =
                notificationRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(
                        currentUser.getId()
                );

        notifications.forEach(notification -> notification.setIsRead(true));

        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        validateNotificationOwner(notification, currentUser);

        notificationRepository.delete(notification);
    }

    private void validateNotificationOwner(Notification notification, AppUser currentUser) {
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new NotResourceOwnerException(
                    "You are not allowed to access this notification"
            );
        }
    }
}
