package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.AttachPhotoRequest;
import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.request.UpdatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;
import com.example.propertyrentalmanagement.services.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

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

    // TODO: Search properties
    // TODO: Add pagination
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
}
