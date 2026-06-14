package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceScheduleRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceScheduleResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Maintenance;
import com.example.propertyrentalmanagement.entitites.MaintenanceSchedule;
import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleFrequency;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleStatus;
import com.example.propertyrentalmanagement.enums.MaintenanceStatus;
import com.example.propertyrentalmanagement.enums.Urgency;
import com.example.propertyrentalmanagement.exceptions.MaintenanceScheduleNotFoundException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.PropertyNotFound;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.MaintenanceRepository;
import com.example.propertyrentalmanagement.repositories.MaintenanceScheduleRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.MaintenanceScheduleService;
import lombok.RequiredArgsConstructor;
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

        maintenanceRepository.save(maintenance);
        // TODO: Check if can be created depending on availability calendar, on task: on task: [SPL-17] Calendario de disponibilidad sincronizado
        // check if in that day there is a reservation

        maintenanceScheduleFound.setLastCompletedAt(LocalDateTime.now());
        maintenanceScheduleFound.setNextScheduledDate(calculateNextScheduledDate(
                maintenanceScheduleFound.getNextScheduledDate(),
                maintenanceScheduleFound.getFrequency(),
                maintenanceScheduleFound.getInterval()
        ));

        maintenanceScheduleRepository.save(maintenanceScheduleFound);
    }

    @Override
    public List<MaintenanceScheduleResponse> getMaintenanceSchedulesByPropertyId(UUID propertyId) {
        AppUser authUser = authProvider.getCurrentUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        if (!property.getLandlord().getId().equals(authUser.getId())) {
            throw new NotResourceOwnerException("User is not the landlord of the property");
        }

        List<MaintenanceSchedule> maintenanceSchedules = maintenanceScheduleRepository.findByPropertyId(propertyId);
        return maintenanceSchedules.stream().map(MaintenanceScheduleResponse::fromEntity).toList();
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
