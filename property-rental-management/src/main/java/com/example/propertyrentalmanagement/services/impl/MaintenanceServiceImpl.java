package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Maintenance;
import com.example.propertyrentalmanagement.entitites.MaintenancePhoto;
import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.enums.MaintenancePhotoType;
import com.example.propertyrentalmanagement.enums.MaintenanceStatus;
import com.example.propertyrentalmanagement.exceptions.MaintenanceNotFoundException;
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
    public MaintenanceResponse confirmMaintenance(UUID maintenanceId) {
        return null;
    }

    @Override
    public MaintenanceResponse resolveMaintenance(UUID maintenanceId) {
        return null;
    }
}
