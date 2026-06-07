package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;
import com.example.propertyrentalmanagement.services.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    // TODO: Get reportedId from auth token instead of path param, on task: [SPL-31] Authentication y Authorization, incluyendo Roles
    @PostMapping
    ResponseEntity<GenericResponse> createMaintenance(
            @RequestParam(name = "reportedId") UUID reportedId,
            @Valid @RequestBody CreateMaintenanceRequest maintenanceRequest
    ) {
        MaintenanceResponse createdMaintenance = maintenanceService.createMaintenance(reportedId, maintenanceRequest);

        return GenericResponse.builder()
                .message("Maintenance created successfully")
                .data(createdMaintenance)
                .resourceId(createdMaintenance.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @GetMapping
    ResponseEntity<GenericResponse> getAllMaintenances() {
        List<MaintenanceResponse> maintenances = maintenanceService.getAllMaintenances();
        return GenericResponse.builder()
                .message("Maintenances found")
                .data(maintenances)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/{id}")
    ResponseEntity<GenericResponse> getMaintenanceById(@PathVariable UUID id) {
        MaintenanceResponse maintenanceFound = maintenanceService.getMaintenanceById(id);
        return GenericResponse.builder()
                .message("Maintenance found")
                .data(maintenanceFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
