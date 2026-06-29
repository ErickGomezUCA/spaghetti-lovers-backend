package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.ReviewIdentityDocumentRequest;
import com.example.propertyrentalmanagement.dto.request.SubmitIdentityDocumentRequest;
import com.example.propertyrentalmanagement.dto.response.IdentityDocumentResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.IdentityDocument;
import com.example.propertyrentalmanagement.entitites.Notification;
import com.example.propertyrentalmanagement.enums.DocumentStatus;
import com.example.propertyrentalmanagement.enums.NotificationType;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.ConflictException;
import com.example.propertyrentalmanagement.exceptions.IdentityDocumentNotFoundException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.IdentityDocumentRepository;
import com.example.propertyrentalmanagement.repositories.NotificationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.IdentityDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IdentityDocumentServiceImpl implements IdentityDocumentService {

    private final IdentityDocumentRepository identityDocumentRepository;
    private final AppUserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public IdentityDocumentResponse submitDocument(SubmitIdentityDocumentRequest request) {
        UUID currentUserId = authenticatedUserProvider.getCurrentUser().getId();

        AppUser lockedUser = userRepository.findByIdWithLock(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (lockedUser.getRole() != UserRole.TENANT && lockedUser.getRole() != UserRole.LANDLORD) {
            throw new NotResourceOwnerException("Permission denied: only tenants or landlords can submit identity documents.");
        }

        if (identityDocumentRepository.existsByUserAndDocumentStatus(lockedUser, DocumentStatus.VERIFIED)) {
            throw new ConflictException("Your identity is already verified.");
        }

        if (identityDocumentRepository.existsByUserAndDocumentStatus(lockedUser, DocumentStatus.PENDING)) {
            throw new ConflictException("You already have an identity verification pending.");
        }

        IdentityDocument newDocument = IdentityDocument.builder()
                .user(lockedUser)
                .documentUrl(request.documentUrl())
                .documentStatus(DocumentStatus.PENDING)
                .build();

        newDocument = identityDocumentRepository.save(newDocument);

        List<AppUser> admins = userRepository.findByRole(UserRole.ADMIN);

        if (!admins.isEmpty()) {
            List<Notification> notifications = admins.stream()
                    .map(admin -> Notification.builder()
                            .user(admin)
                            .type(NotificationType.INFO)
                            .title("Nueva verificación pendiente")
                            .message("El usuario "
                                    + lockedUser.getName()
                                    + " - "
                                    + lockedUser.getEmail()
                                    + " subió un nuevo documento de identidad para revisión.")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build()
                    ).toList();
            notificationRepository.saveAll(notifications);
        }

        return IdentityDocumentResponse.fromEntity(newDocument);
    }

    @Override
    @Transactional
    public IdentityDocumentResponse reviewDocument(UUID documentId, ReviewIdentityDocumentRequest request) {
        AppUser currentAdmin = authenticatedUserProvider.getCurrentUser();

        IdentityDocument document = identityDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IdentityDocumentNotFoundException("Identity document not found."));

        if (document.getDocumentStatus() != DocumentStatus.PENDING) {
            throw new ConflictException("This document has already been reviewed and cannot be modified.");
        }

        if (request.documentStatus() == DocumentStatus.PENDING) {
            throw new ConflictException("Cannot set document status back to PENDING.");
        }

        document.setDocumentStatus(request.documentStatus());
        document.setReviewedBy(currentAdmin);
        identityDocumentRepository.save(document);

        String statusMessage;
        if (request.documentStatus() == DocumentStatus.VERIFIED) {
            statusMessage = "Your identity verification has been approved successfully.";
        } else if (request.rejectionReason() != null && !request.rejectionReason().isBlank()) {
            statusMessage = "Your identity verification was rejected. Reason: " + request.rejectionReason();
        } else {
            statusMessage = "Your identity verification was rejected. Please upload a valid document.";
        }

        Notification notification = Notification.builder()
                .user(document.getUser())
                .type(NotificationType.INFO)
                .title("Identity Verification Update")
                .message(statusMessage)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        return IdentityDocumentResponse.fromEntity(document);
    }

    @Override
    public List<IdentityDocumentResponse> getAllDocuments(DocumentStatus status) {
        List<IdentityDocument> documents = (status != null)
                ? identityDocumentRepository.findByDocumentStatus(status)
                : identityDocumentRepository.findAll();

        return documents.stream()
                .map(IdentityDocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}