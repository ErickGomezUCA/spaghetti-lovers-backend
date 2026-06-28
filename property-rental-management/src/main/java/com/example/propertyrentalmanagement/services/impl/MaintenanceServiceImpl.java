package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.ConfirmMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.ResolveMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.*;
import com.example.propertyrentalmanagement.exceptions.CalendarConflictException;
import com.example.propertyrentalmanagement.exceptions.MaintenanceNotFoundException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.repositories.*;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.MaintenanceService;
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private static final DateTimeFormatter HTML_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private final MaintenanceRepository maintenanceRepository;
    private final MaintenancePhotoRepository maintenancePhotoRepository;
    private final ReservationRepository reservationRepository;
    private final AvailabilityCalendarRepository availabilityCalendarRepository;
    private final NotificationRepository notificationRepository;
    private final AuthenticatedUserProvider authProvider;

    @Override
    public MaintenanceResponse createMaintenance(CreateMaintenanceRequest maintenanceRequest) {
        AppUser authUser = authProvider.getCurrentUser();

        UUID reservationId = maintenanceRequest.reservationId();
        Reservation reservationFound = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        Property property = reservationFound.getProperty();

        if (!reservationFound.getTenant().getId().equals(authUser.getId())) {
            throw new RuntimeException("User is not part of the reservation");
        }

        Maintenance maintenance = Maintenance.builder()
                .property(property)
                .reservation(reservationFound)
                .reportedBy(authUser)
                .title(maintenanceRequest.title())
                .description(maintenanceRequest.description())
                .urgency(maintenanceRequest.urgency())
                .maintenanceStatus(MaintenanceStatus.SCHEDULED)
                .build();

        Maintenance createdMaintenance = maintenanceRepository.save(maintenance);

        if (maintenanceRequest.photoUrls() != null && !maintenanceRequest.photoUrls().isEmpty()) {
            List<MaintenancePhoto> photos = maintenanceRequest.photoUrls().stream()
                    .filter(Objects::nonNull)
                    .filter(entry -> entry.url() != null)
                    .map(entry -> MaintenancePhoto.builder()
                            .maintenance(createdMaintenance)
                            .url(entry.url())
                            .cloudinaryPublicId(entry.publicId())
                            .photoType(MaintenancePhotoType.REQUEST)
                            .build())
                    .toList();

            if (!photos.isEmpty()) {
                maintenancePhotoRepository.saveAll(photos);
            }
        }

        Notification createNotification = Notification.builder()
                .user(property.getLandlord())
                .type(NotificationType.MAINTENANCE)
                .title("Nueva solicitud de mantenimiento")
                .message("Nueva solicitud: \"" + maintenanceRequest.title() + "\" con urgencia " + maintenanceRequest.urgency() + ".")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(createNotification);

        return MaintenanceResponse.fromEntity(createdMaintenance);
    }

    @Override
    public MaintenanceResponse getMaintenanceById(UUID maintenanceId) {
        AppUser authUser = authProvider.getCurrentUser();
        Maintenance maintenanceFound = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new MaintenanceNotFoundException("Maintenance not found"));

        // If TENANT and user is not part of the reservation, OR if LANDLORD and user is not owner of the property: do not show maintenance requests
        if ((authUser.getRole() == UserRole.TENANT && !maintenanceFound.getReportedBy().getId().equals(authUser.getId()))
                || (authUser.getRole() == UserRole.LANDLORD && !maintenanceFound.getProperty().getLandlord().getId().equals(authUser.getId()))
        ) {
            throw new MaintenanceNotFoundException("Maintenance not found");
        }

        return MaintenanceResponse.fromEntity(maintenanceFound);
    }

    @Override
    public Page<MaintenanceResponse> getAllMaintenances(int page, int pageSize, String sortBy, String sortOrder) {
        AppUser authUser = authProvider.getCurrentUser();
        Pageable pageable = PaginationUtils.getPageRequest(page, pageSize, sortBy, sortOrder);
        Page<Maintenance> maintenances = Page.empty();

        if (authUser.getRole() == UserRole.TENANT) {
            maintenances = maintenanceRepository.findAllByReportedById(authUser.getId(), pageable);
        } else if (authUser.getRole() == UserRole.LANDLORD) {
            maintenances = maintenanceRepository.findAllByPropertyLandlordId(authUser.getId(), pageable);
        } else if (authUser.getRole() == UserRole.ADMIN) {
            maintenances = maintenanceRepository.findAll(pageable);
        }

        return maintenances.map(MaintenanceResponse::fromEntity);
    }

    @Override
    public MaintenanceResponse confirmMaintenance(UUID maintenanceId, ConfirmMaintenanceRequest confirmMaintenanceRequest) {
        AppUser authUser = authProvider.getCurrentUser();

        Maintenance maintenanceFound = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new MaintenanceNotFoundException("Maintenance not found"));

        if (!maintenanceFound.getProperty().getLandlord().getId().equals(authUser.getId())) {
            throw new NotResourceOwnerException("User is not landlord of this property");
        }

        LocalDateTime scheduledStart = LocalDateTime.parse(confirmMaintenanceRequest.scheduledStart(), HTML_DATETIME);
        LocalDateTime scheduledEnd = LocalDateTime.parse(confirmMaintenanceRequest.scheduledEnd(), HTML_DATETIME);

        List<AvailabilityCalendar> overlaps = availabilityCalendarRepository.findOverlappingBlocks(
                maintenanceFound.getProperty().getId(), scheduledStart, scheduledEnd);

        if (!overlaps.isEmpty()) {
            throw new CalendarConflictException("El rango de fechas seleccionado ya está ocupado por otro evento en el calendario.");
        }

        maintenanceFound.setScheduledStart(scheduledStart);
        maintenanceFound.setScheduledEnd(scheduledEnd);
        maintenanceFound.setMaintenanceStatus(MaintenanceStatus.RESOLVING);
        Maintenance updatedMaintenance = maintenanceRepository.save(maintenanceFound);

        if (confirmMaintenanceRequest.blockCalendar()) {
            AvailabilityCalendar calendarBlock = AvailabilityCalendar.builder()
                    .property(updatedMaintenance.getProperty())
                    .timestampStart(scheduledStart)
                    .timestampEnd(scheduledEnd)
                    .blockType(BlockType.MAINTENANCE)
                    .maintenance(updatedMaintenance)
                    .blockedReason(updatedMaintenance.getTitle())
                    .build();
            availabilityCalendarRepository.save(calendarBlock);
        }

        Notification confirmNotification = Notification.builder()
                .user(updatedMaintenance.getReportedBy())
                .type(NotificationType.MAINTENANCE)
                .title("Mantenimiento confirmado")
                .message("Tu solicitud \"" + updatedMaintenance.getTitle() + "\" ha sido confirmada.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(confirmNotification);

        return MaintenanceResponse.fromEntity(updatedMaintenance);
    }

    @Override
    @Transactional
    public MaintenanceResponse resolveMaintenance(UUID maintenanceId, ResolveMaintenanceRequest resolveMaintenanceRequest) {
        AppUser authUser = authProvider.getCurrentUser();

        Maintenance maintenanceFound = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new MaintenanceNotFoundException("Maintenance not found"));

        if (!maintenanceFound.getProperty().getLandlord().getId().equals(authUser.getId())) {
            throw new NotResourceOwnerException("User is not landlord of this property");
        }

        if (resolveMaintenanceRequest.photoUrls() != null && !resolveMaintenanceRequest.photoUrls().isEmpty()) {
            List<MaintenancePhoto> photos = resolveMaintenanceRequest.photoUrls().stream()
                    .filter(Objects::nonNull)
                    .filter(entry -> entry.url() != null)
                    .map(entry -> MaintenancePhoto.builder()
                            .maintenance(maintenanceFound)
                            .url(entry.url())
                            .cloudinaryPublicId(entry.publicId())
                            .photoType(MaintenancePhotoType.RESPONSE)
                            .build())
                    .toList();

            if (!photos.isEmpty()) {
                maintenancePhotoRepository.saveAll(photos);
            }
        }

        availabilityCalendarRepository.deleteByMaintenance(maintenanceFound);

        maintenanceFound.setMaintenanceStatus(MaintenanceStatus.RESOLVED);
        maintenanceFound.setResolutionNotes(resolveMaintenanceRequest.resolutionNotes());
        Maintenance updatedMaintenance = maintenanceRepository.save(maintenanceFound);

        Notification resolveNotification = Notification.builder()
                .user(updatedMaintenance.getReportedBy())
                .type(NotificationType.MAINTENANCE)
                .title("Mantenimiento resuelto")
                .message("Tu solicitud \"" + updatedMaintenance.getTitle() + "\" ha sido resuelta.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(resolveNotification);

        return MaintenanceResponse.fromEntity(updatedMaintenance);
    }
}
