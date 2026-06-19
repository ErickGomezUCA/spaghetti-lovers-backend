package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
