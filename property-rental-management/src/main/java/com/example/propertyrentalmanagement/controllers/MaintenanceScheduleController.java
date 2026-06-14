package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceScheduleRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.MaintenanceScheduleResponse;
import com.example.propertyrentalmanagement.services.MaintenanceScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance-schedules")
@RequiredArgsConstructor
public class MaintenanceScheduleController {
    private final MaintenanceScheduleService maintenanceScheduleService;

    @PreAuthorize("@authorizationService.isLandlord()")
    @PostMapping
    ResponseEntity<GenericResponse> createMaintenanceSchedule(@Valid @RequestBody CreateMaintenanceScheduleRequest createMaintenanceScheduleRequest) {
        MaintenanceScheduleResponse response = maintenanceScheduleService.createMaintenanceSchedule(createMaintenanceScheduleRequest);
        return GenericResponse.builder()
                .message("Maintenance schedule created successfully")
                .data(response)
                .resourceId(response.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @PostMapping("/{id}")
    ResponseEntity<GenericResponse> startMaintenanceSchedule(@PathVariable UUID id) {
        maintenanceScheduleService.startMaintenanceSchedule(id);
        return GenericResponse.builder()
                .message("Maintenance schedule started successfully")
                .status(HttpStatus.OK) // TODO: Include maintenance resource location here
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @GetMapping("/property/{propertyId}")
    ResponseEntity<GenericResponse> getMaintenanceSchedulesByPropertyId(@PathVariable UUID propertyId) {
        List<MaintenanceScheduleResponse> maintenanceSchedules = maintenanceScheduleService.getMaintenanceSchedulesByPropertyId(propertyId);
        return GenericResponse.builder()
                .message("Maintenance found")
                .data(maintenanceSchedules)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

}
