package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceScheduleRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceScheduleResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MaintenanceScheduleService {
    MaintenanceScheduleResponse createMaintenanceSchedule(CreateMaintenanceScheduleRequest createMaintenanceScheduleRequest);

    void startMaintenanceSchedule(UUID id);

    Page<MaintenanceScheduleResponse> getMaintenanceSchedulesByPropertyId(int page, int pageSize, String sortBy, String sortOrder, UUID propertyId);
}
