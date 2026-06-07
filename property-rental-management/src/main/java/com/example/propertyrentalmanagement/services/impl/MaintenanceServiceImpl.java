package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.ConfirmMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.ResolveMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Maintenance;
import com.example.propertyrentalmanagement.entitites.MaintenancePhoto;
import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.enums.MaintenancePhotoType;
import com.example.propertyrentalmanagement.enums.MaintenanceStatus;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.MaintenanceNotFoundException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.PropertyNotFound;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.MaintenancePhotoRepository;
import com.example.propertyrentalmanagement.repositories.MaintenanceRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.services.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final MaintenancePhotoRepository maintenancePhotoRepository;
    private final AppUserRepository appUserRepository;
    private final PropertyRepository propertyRepository;
    // TODO: Implement reservationRepository here, on task: [SPL-18] Reservas con fechas fijas (check-in/out)

    @Override
    public MaintenanceResponse createMaintenance(UUID reportedId, CreateMaintenanceRequest maintenanceRequest) {
        UUID propertyId = maintenanceRequest.propertyId();
        UUID reservationId = maintenanceRequest.reservationId();

        AppUser reportedUserFound = appUserRepository.findById(reportedId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Property propertyFound = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

//        TODO: Check if user is current tenant of the reservation, on task: [SPL-18] Reservas con fechas fijas (check-in/out)

        if (reportedUserFound.getRole() == UserRole.LANDLORD && propertyFound.getLandlord().getId() != reportedId) {
            throw new NotResourceOwnerException("User is not the landlord of this property");
        }

        Maintenance maintenance = Maintenance.builder()
                .property(propertyFound)
                .reservation(null)
                .reportedBy(reportedUserFound)
                .title(maintenanceRequest.title())
                .description(maintenanceRequest.description())
                .urgency(maintenanceRequest.urgency())
                .maintenanceStatus(MaintenanceStatus.SCHEDULED)
                .build();

        Maintenance createdMaintenance = maintenanceRepository.save(maintenance);

        if (maintenanceRequest.photoUrls() != null && !maintenanceRequest.photoUrls().isEmpty()) {
            List<MaintenancePhoto> photos = maintenanceRequest.photoUrls().stream()
                    .filter(Objects::nonNull)
                    .map(url -> MaintenancePhoto.builder()
                            .maintenance(createdMaintenance)
                            .url(url)
                            .photoType(MaintenancePhotoType.REQUEST)
                            .build())
                    .toList();

            if (!photos.isEmpty()) {
                maintenancePhotoRepository.saveAll(photos);
            }
        }

        // TODO: Insert notification to landlord, on task: [SPL-18] Reservas con fechas fijas (check-in/out)

        return MaintenanceResponse.fromEntity(createdMaintenance);
    }

    @Override
    public MaintenanceResponse getMaintenanceById(UUID maintenanceId) {
        Maintenance maintenanceFound = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new MaintenanceNotFoundException("Maintenance not found"));
        return MaintenanceResponse.fromEntity(maintenanceFound);
    }

    @Override
    public List<MaintenanceResponse> getAllMaintenances() {
        List<Maintenance> maintenances = maintenanceRepository.findAll();
        return maintenances.stream().map(MaintenanceResponse::fromEntity).toList();
    }

    @Override
    public MaintenanceResponse confirmMaintenance(UUID landlordId, UUID maintenanceId, ConfirmMaintenanceRequest confirmMaintenanceRequest) {
        Maintenance maintenanceFound = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new MaintenanceNotFoundException("Maintenance not found"));

        Property property = propertyRepository.findById(maintenanceFound.getProperty().getId())
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        // TODO: Check ownership

        maintenanceFound.setMaintenanceStatus(MaintenanceStatus.RESOLVING);
        Maintenance updatedMaintenance = maintenanceRepository.save(maintenanceFound);

        // TODO: Block maintenance into availability calendar, on task: [SPL-17] Calendario de disponibilidad sincronizado
        return MaintenanceResponse.fromEntity(updatedMaintenance);
    }

    @Override
    public MaintenanceResponse resolveMaintenance(UUID landlordId, UUID maintenanceId, ResolveMaintenanceRequest resolveMaintenanceRequest) {
        Maintenance maintenanceFound = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new MaintenanceNotFoundException("Maintenance not found"));

        Property property = propertyRepository.findById(maintenanceFound.getProperty().getId())
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        // TODO: Check ownership

        if (resolveMaintenanceRequest.photoUrls() != null && !resolveMaintenanceRequest.photoUrls().isEmpty()) {
            List<MaintenancePhoto> photos = resolveMaintenanceRequest.photoUrls().stream()
                    .filter(Objects::nonNull)
                    .map(url -> MaintenancePhoto.builder()
                            .maintenance(maintenanceFound)
                            .url(url)
                            .photoType(MaintenancePhotoType.RESPONSE)
                            .build())
                    .toList();

            if (!photos.isEmpty()) {
                maintenancePhotoRepository.saveAll(photos);
            }
        }

        maintenanceFound.setMaintenanceStatus(MaintenanceStatus.RESOLVED);
        maintenanceFound.setResolutionNotes(resolveMaintenanceRequest.resolutionNotes());
        Maintenance updatedMaintenance = maintenanceRepository.save(maintenanceFound);

        return MaintenanceResponse.fromEntity(updatedMaintenance);
    }
}
