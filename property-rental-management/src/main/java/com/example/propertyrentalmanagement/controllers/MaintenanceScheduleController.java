package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceScheduleRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.MaintenanceScheduleResponse;
import com.example.propertyrentalmanagement.services.MaintenanceScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance-schedules")
@RequiredArgsConstructor
public class MaintenanceScheduleController {
    private final MaintenanceScheduleService maintenanceScheduleService;

    @PostMapping
    ResponseEntity<GenericResponse> createMaintenanceSchedule(@RequestParam(name = "scheduledById") UUID scheduledById, @Valid @RequestBody CreateMaintenanceScheduleRequest request) {
        MaintenanceScheduleResponse response = maintenanceScheduleService.createMaintenanceSchedule(scheduledById, request);
        return GenericResponse.builder()
                .message("Maintenance schedule created successfully")
                .data(response)
                .resourceId(response.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @PostMapping("/{id}")
    ResponseEntity<GenericResponse> startMaintenanceSchedule(@PathVariable UUID id) {
        maintenanceScheduleService.startMaintenanceSchedule(id);
        return GenericResponse.builder()
                .message("Maintenance schedule started successfully")
                .status(HttpStatus.OK) // TODO: Include maintenance resource location here
                .build().buildResponse();
    }

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
