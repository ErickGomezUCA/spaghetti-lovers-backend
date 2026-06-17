package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.ConfirmMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.CreateMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.request.ResolveMaintenanceRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.MaintenanceResponse;
import com.example.propertyrentalmanagement.dto.response.PaginationMeta;
import com.example.propertyrentalmanagement.services.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    @PreAuthorize("@authorizationService.isTenant()")
    @PostMapping
    ResponseEntity<GenericResponse> createMaintenance(
            @Valid @RequestBody CreateMaintenanceRequest maintenanceRequest
    ) {
        MaintenanceResponse createdMaintenance = maintenanceService.createMaintenance(maintenanceRequest);

        return GenericResponse.builder()
                .message("Maintenance created successfully")
                .data(createdMaintenance)
                .resourceId(createdMaintenance.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @GetMapping
    ResponseEntity<GenericResponse> getAllMaintenances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<MaintenanceResponse> maintenances = maintenanceService.getAllMaintenances(page, pageSize, sortBy, sortOrder);
        return GenericResponse.builder()
                .message("Maintenances found")
                .data(maintenances.getContent())
                .pagination(PaginationMeta.fromPage(maintenances))
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

    @PreAuthorize("@authorizationService.isLandlord()")
    @PatchMapping("/{id}/confirm")
    ResponseEntity<GenericResponse> confirmMaintenance(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmMaintenanceRequest confirmMaintenanceRequest) {
        MaintenanceResponse confirmedMaintenance = maintenanceService.confirmMaintenance(id, confirmMaintenanceRequest);
        return GenericResponse.builder()
                .message("Maintenance confirmed")
                .data(confirmedMaintenance)
                .status(HttpStatus.CREATED)
                .resourceId(confirmedMaintenance.id())
                .build().buildResponse();
    }

    @PatchMapping("/{id}/resolve")
    ResponseEntity<GenericResponse> resolveMaintenance(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveMaintenanceRequest resolveMaintenanceRequest) {
        MaintenanceResponse confirmedMaintenance = maintenanceService.resolveMaintenance(id, resolveMaintenanceRequest);
        return GenericResponse.builder()
                .message("Maintenance resolved")
                .data(confirmedMaintenance)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
