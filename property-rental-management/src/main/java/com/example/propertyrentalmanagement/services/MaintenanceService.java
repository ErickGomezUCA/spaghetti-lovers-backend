package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService {
    MaintenanceResponse createMaintenance(UUID reportedId, CreateMaintenanceRequest maintenanceRequest);

    MaintenanceResponse getMaintenanceById(UUID maintenanceId);

    List<MaintenanceResponse> getAllMaintenances();

    MaintenanceResponse confirmMaintenance(UUID maintenanceId);

    MaintenanceResponse resolveMaintenance(UUID maintenanceId);
}
