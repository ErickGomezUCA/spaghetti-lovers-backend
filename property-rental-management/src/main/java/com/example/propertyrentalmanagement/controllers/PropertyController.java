package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.AttachPhotoRequest;
import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.request.UpdatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.AvailabilityResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.PropertyReportResponse;
import com.example.propertyrentalmanagement.dto.response.PaginationMeta;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;
import com.example.propertyrentalmanagement.enums.PropertyStatus;
import com.example.propertyrentalmanagement.enums.PropertyType;
import com.example.propertyrentalmanagement.services.AvailabilityService;
import com.example.propertyrentalmanagement.services.PropertyService;
import com.example.propertyrentalmanagement.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;
    private final AvailabilityService availabilityService;
    private final ReportService reportService;

    @PreAuthorize("@authorizationService.isLandlord()")
    @PostMapping
    ResponseEntity<GenericResponse> createProperty(
            @Valid @RequestBody CreatePropertyRequest propertyRequest
    ) {
        PropertyResponse createdProperty = propertyService.createProperty(propertyRequest);

        return GenericResponse.builder()
                .message("Property created successfully")
                .data(createdProperty)
                .resourceId(createdProperty.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @PostMapping("/attach-photos/{id}")
    ResponseEntity<GenericResponse> attachPhotosToProperty(
            @PathVariable UUID id,
            @Valid @RequestBody AttachPhotoRequest attachPhotoRequest
    ) {
        PropertyResponse propertyResponse = propertyService.attachPhotosToProperty(id, attachPhotoRequest);

        return GenericResponse.builder()
                .message("Property photos created successfully")
                .data(propertyResponse)
                .resourceId(propertyResponse.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @GetMapping
    ResponseEntity<GenericResponse> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) Integer minGuests,
            @RequestParam(required = false) PropertyStatus status
    ) {
        Page<PropertyResponse> properties = propertyService.getAllProperties(
                page, pageSize, sortBy, sortOrder, term, propertyType, minGuests, status);
        return GenericResponse.builder()
                .message("Properties found")
                .data(properties.getContent())
                .pagination(PaginationMeta.fromPage(properties))
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/{id}")
    ResponseEntity<GenericResponse> getPropertyById(@PathVariable UUID id) {
        PropertyResponse propertyFound = propertyService.getPropertyById(id);
        return GenericResponse.builder()
                .message("Property found")
                .data(propertyFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/landlord/{landlordId}")
    ResponseEntity<GenericResponse> getPropertiesByLandlordId(
            @PathVariable UUID landlordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<PropertyResponse> properties = propertyService.getPropertiesByLandlordId(landlordId, page, pageSize, sortBy, sortOrder);
        return GenericResponse.builder()
                .message("Properties found")
                .data(properties.getContent())
                .pagination(PaginationMeta.fromPage(properties))
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @PutMapping("/{id}")
    ResponseEntity<GenericResponse> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest propertyRequest
    ) {
        PropertyResponse updatedProperty = propertyService.updateProperty(id, propertyRequest);

        return GenericResponse.builder()
                .message("Property updated successfully")
                .data(updatedProperty)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @DeleteMapping("/{id}")
    ResponseEntity<GenericResponse> deleteProperty(
            @PathVariable UUID id
    ) {
        propertyService.deleteProperty(id);
        return GenericResponse.builder()
                .message("Property deleted successfully")
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/{id}/availability")
    ResponseEntity<GenericResponse> checkAvailability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        AvailabilityResponse availability = availabilityService.checkAvailability(id, startDate, endDate);
        return GenericResponse.builder()
                .message("Availability checked successfully")
                .data(availability)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord() or @authorizationService.isAdmin()")
    @GetMapping("/{id}/report")
    ResponseEntity<GenericResponse> getPropertyReport(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        PropertyReportResponse report = reportService.getPropertyReport(id, startDate, endDate);
        return GenericResponse.builder()
                .message("Property report generated successfully")
                .data(report)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
