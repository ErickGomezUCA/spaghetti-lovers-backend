package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceScheduleRequest;
import com.example.propertyrentalmanagement.dto.response.MaintenanceScheduleResponse;

import java.util.List;
import java.util.UUID;

public interface MaintenanceScheduleService {
    MaintenanceScheduleResponse createMaintenanceSchedule(CreateMaintenanceScheduleRequest createMaintenanceScheduleRequest);

    void startMaintenanceSchedule(UUID id);

    List<MaintenanceScheduleResponse> getMaintenanceSchedulesByPropertyId(UUID propertyId);
}
