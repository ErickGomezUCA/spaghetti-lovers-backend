package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    long countByUser_IdAndIsReadFalse(UUID userId);

    Page<Notification> findByUser_Id(UUID userId, Pageable pageable);

    Page<Notification> findByUser_IdAndIsReadFalse(UUID userId, Pageable pageable);
}
