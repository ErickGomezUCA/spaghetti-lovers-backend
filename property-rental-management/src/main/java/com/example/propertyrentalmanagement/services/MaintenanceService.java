package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.ConfirmMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.ResolveMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MaintenanceService {
    MaintenanceResponse createMaintenance(CreateMaintenanceRequest maintenanceRequest);

    MaintenanceResponse getMaintenanceById(UUID maintenanceId);

    Page<MaintenanceResponse> getAllMaintenances(int page, int pageSize, String sortBy, String sortOrder);

    MaintenanceResponse confirmMaintenance(UUID maintenanceId, ConfirmMaintenanceRequest confirmMaintenanceRequest);

    MaintenanceResponse resolveMaintenance(UUID maintenanceId, ResolveMaintenanceRequest resolveMaintenanceRequest);
}
