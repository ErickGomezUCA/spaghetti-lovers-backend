package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;
import com.example.propertyrentalmanagement.services.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

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
}
