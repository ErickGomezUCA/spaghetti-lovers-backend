package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceScheduleRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceScheduleResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.AvailabilityCalendar;
import com.example.propertyrentalmanagement.entitites.Maintenance;
import com.example.propertyrentalmanagement.entitites.MaintenanceSchedule;
import com.example.propertyrentalmanagement.entitites.Notification;
import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.enums.BlockType;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleFrequency;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleStatus;
import com.example.propertyrentalmanagement.enums.MaintenanceStatus;
import com.example.propertyrentalmanagement.enums.NotificationType;
import com.example.propertyrentalmanagement.enums.Urgency;
import com.example.propertyrentalmanagement.exceptions.MaintenanceScheduleNotFoundException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.PropertyNotFound;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.AvailabilityCalendarRepository;
import com.example.propertyrentalmanagement.repositories.MaintenanceRepository;
import com.example.propertyrentalmanagement.repositories.MaintenanceScheduleRepository;
import com.example.propertyrentalmanagement.repositories.NotificationRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.MaintenanceScheduleService;
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceScheduleServiceImpl implements MaintenanceScheduleService {
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final AppUserRepository appUserRepository;
    private final PropertyRepository propertyRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final AvailabilityCalendarRepository availabilityCalendarRepository;
    private final NotificationRepository notificationRepository;
    private final AuthenticatedUserProvider authProvider;

    @Override
    public MaintenanceScheduleResponse createMaintenanceSchedule(CreateMaintenanceScheduleRequest request) {
        AppUser authUser = authProvider.getCurrentUser();

        UUID propertyId = request.propertyId();
        Property propertyFound = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        if (!propertyFound.getLandlord().getId().equals(authUser.getId())) {
            throw new NotResourceOwnerException("User is not the landlord of the property");
        }

        MaintenanceSchedule maintenanceSchedule = MaintenanceSchedule.builder()
                .property(propertyFound)
                .scheduledBy(authUser)
                .title(request.title())
                .description(request.description())
                .frequency(request.frequency())
                .interval(request.interval())
                .nextScheduledDate(LocalDateTime.parse(request.nextScheduledDate()))
                .status(MaintenanceScheduleStatus.SCHEDULED)
                .build();

        MaintenanceSchedule createdMaintenanceSchedule = maintenanceScheduleRepository.save(maintenanceSchedule);
        return MaintenanceScheduleResponse.fromEntity(createdMaintenanceSchedule);
    }

    @Override
    public void startMaintenanceSchedule(UUID id) {
        MaintenanceSchedule maintenanceScheduleFound = maintenanceScheduleRepository.findById(id)
                .orElseThrow(() -> new MaintenanceScheduleNotFoundException("Maintenance schedule not found"));

        AppUser authUser = authProvider.getCurrentUser();

        if (!maintenanceScheduleFound.getProperty().getLandlord().getId().equals(authUser.getId())) {
            throw new NotResourceOwnerException("User is not the landlord of the property");
        }

        Maintenance maintenance = Maintenance.builder()
                .property(maintenanceScheduleFound.getProperty())
                .reportedBy(maintenanceScheduleFound.getScheduledBy())
                .title(maintenanceScheduleFound.getTitle())
                .description(maintenanceScheduleFound.getDescription())
                .urgency(Urgency.LOW)
                .maintenanceStatus(MaintenanceStatus.SCHEDULED)
                .build();

        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        LocalDateTime blockStart = maintenanceScheduleFound.getNextScheduledDate();
        LocalDateTime blockEnd = blockStart.plusDays(1);
        List<AvailabilityCalendar> overlaps = availabilityCalendarRepository.findOverlappingBlocks(
                maintenanceScheduleFound.getProperty().getId(), blockStart, blockEnd);
        if (overlaps.isEmpty()) {
            AvailabilityCalendar calendarBlock = AvailabilityCalendar.builder()
                    .property(maintenanceScheduleFound.getProperty())
                    .timestampStart(blockStart)
                    .timestampEnd(blockEnd)
                    .blockType(BlockType.PREVENTIVE_MAINTENANCE)
                    .maintenance(savedMaintenance)
                    .blockedReason("Mantenimiento preventivo: " + maintenanceScheduleFound.getTitle())
                    .build();
            availabilityCalendarRepository.save(calendarBlock);
        }

        Notification scheduleNotification = Notification.builder()
                .user(maintenanceScheduleFound.getScheduledBy())
                .type(NotificationType.MAINTENANCE)
                .title("Mantenimiento preventivo activado")
                .message("El mantenimiento preventivo \"" + maintenanceScheduleFound.getTitle() + "\" ha sido iniciado.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(scheduleNotification);

        maintenanceScheduleFound.setLastCompletedAt(LocalDateTime.now());
        maintenanceScheduleFound.setNextScheduledDate(calculateNextScheduledDate(
                maintenanceScheduleFound.getNextScheduledDate(),
                maintenanceScheduleFound.getFrequency(),
                maintenanceScheduleFound.getInterval()
        ));

        maintenanceScheduleRepository.save(maintenanceScheduleFound);
    }

    @Override
    public Page<MaintenanceScheduleResponse> getMaintenanceSchedulesByPropertyId(int page, int pageSize, String sortBy, String sortOrder, UUID propertyId) {
        AppUser authUser = authProvider.getCurrentUser();
        Pageable pageable = PaginationUtils.getPageRequest(page, pageSize, sortBy, sortOrder);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        if (!property.getLandlord().getId().equals(authUser.getId())) {
            throw new NotResourceOwnerException("User is not the landlord of the property");
        }

        Page<MaintenanceSchedule> maintenanceSchedules = maintenanceScheduleRepository.findAllByPropertyId(propertyId, pageable);
        return maintenanceSchedules.map(MaintenanceScheduleResponse::fromEntity);
    }

    private LocalDateTime calculateNextScheduledDate(
            LocalDateTime currentNextScheduledDate,
            MaintenanceScheduleFrequency frequency,
            int interval
    ) {
        return switch (frequency) {
            case DAILY -> currentNextScheduledDate.plusDays(interval);
            case WEEKLY -> currentNextScheduledDate.plusWeeks(interval);
            case MONTHLY -> currentNextScheduledDate.plusMonths(interval);
            case YEARLY -> currentNextScheduledDate.plusYears(interval);
        };
    }
}
