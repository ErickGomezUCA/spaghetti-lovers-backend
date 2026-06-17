package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.ConfirmMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.ResolveMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.MaintenancePhotoType;
import com.example.propertyrentalmanagement.enums.MaintenanceStatus;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.MaintenanceNotFoundException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.repositories.MaintenancePhotoRepository;
import com.example.propertyrentalmanagement.repositories.MaintenanceRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.MaintenanceService;
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final MaintenancePhotoRepository maintenancePhotoRepository;
    private final ReservationRepository reservationRepository;
    private final AuthenticatedUserProvider authProvider;
    // TODO: Implement reservationRepository here, on task: [SPL-18] Reservas con fechas fijas (check-in/out)

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
                .property(property) // TODO: Remove property? It can be fetched from reservation either way
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

        maintenanceFound.setMaintenanceStatus(MaintenanceStatus.RESOLVING);
        Maintenance updatedMaintenance = maintenanceRepository.save(maintenanceFound);

        // TODO: Block maintenance into availability calendar, on task: [SPL-17] Calendario de disponibilidad sincronizado
        return MaintenanceResponse.fromEntity(updatedMaintenance);
    }

    @Override
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
