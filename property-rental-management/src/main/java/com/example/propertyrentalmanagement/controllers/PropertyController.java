package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.AttachPhotoRequest;
import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.request.UpdatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.AvailabilityResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;
import com.example.propertyrentalmanagement.services.AvailabilityService;
import com.example.propertyrentalmanagement.services.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;
    private final AvailabilityService availabilityService;

    // TODO: Get landlordId from auth token instead of path param, on task: [SPL-31] Authentication y Authorization, incluyendo Roles
    @PostMapping
    ResponseEntity<GenericResponse> createProperty(
            @RequestParam(name = "landlordId") UUID landlordId,
            @Valid @RequestBody CreatePropertyRequest propertyRequest
    ) {
        PropertyResponse createdProperty = propertyService.createProperty(propertyRequest, landlordId);

        return GenericResponse.builder()
                .message("Property created successfully")
                .data(createdProperty)
                .resourceId(createdProperty.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

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
    ResponseEntity<GenericResponse> getAllProperties() {
        List<PropertyResponse> properties = propertyService.getAllProperties();
        return GenericResponse.builder()
                .message("Properties found")
                .data(properties)
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
    ResponseEntity<GenericResponse> getPropertiesByLandlordId(@PathVariable UUID landlordId) {
        List<PropertyResponse> properties = propertyService.getPropertiesByLandlordId(landlordId);
        return GenericResponse.builder()
                .message("Properties found")
                .data(properties)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    // TODO: Get landlordId from auth token instead of path param, on task: [SPL-31] Authentication y Authorization, incluyendo Roles
    @PutMapping("/{id}")
    ResponseEntity<GenericResponse> updateProperty(
            @PathVariable UUID id,
            @RequestParam(name = "landlordId") UUID landlordId,
            @Valid @RequestBody UpdatePropertyRequest propertyRequest
    ) {
        PropertyResponse updatedProperty = propertyService.updateProperty(landlordId, id, propertyRequest);

        return GenericResponse.builder()
                .message("Property updated successfully")
                .data(updatedProperty)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

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
}
