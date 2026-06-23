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
import com.example.propertyrentalmanagement.repositories.IdentityDocumentRepository;
import com.example.propertyrentalmanagement.repositories.NotificationRepository;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.IdentityDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        if (currentUser.getRole() != UserRole.TENANT && currentUser.getRole() != UserRole.LANDLORD) {
            throw new NotResourceOwnerException("Permission denied: only tenants or landlords can submit identity documents.");
        }

        if (identityDocumentRepository.existsByUserAndDocumentStatus(currentUser, DocumentStatus.VERIFIED)) {
            throw new ConflictException("Your identity is already verified.");
        }

        if (identityDocumentRepository.existsByUserAndDocumentStatus(currentUser, DocumentStatus.PENDING)) {
            throw new ConflictException("You already have an identity verification pending.");
        }

        IdentityDocument newDocument = IdentityDocument.builder()
                .user(currentUser)
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
                            .title("New Identity Verification Pending")
                            .message("User " + currentUser.getName() + " - " + currentUser.getEmail() + " has submitted a new identity document for verification.")
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

        if (currentAdmin.getRole() != com.example.propertyrentalmanagement.enums.UserRole.ADMIN) {
            throw new NotResourceOwnerException("Unauthorized: Only administrators can review identity documents.");
        }

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

        String statusMessage = request.documentStatus() == DocumentStatus.VERIFIED
                ? "Your identity verification has been approved successfully."
                : "Your identity verification was rejected. Please upload a valid document.";

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
}