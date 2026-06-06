package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;

import java.util.List;
import java.util.UUID;

public interface PropertyService {
    PropertyResponse createProperty(CreatePropertyRequest propertyRequest, UUID landlordId);

    PropertyResponse getPropertyById(UUID propertyId);

    List<PropertyResponse> getAllProperties();

    List<PropertyResponse> getPropertiesByLandlordId(UUID landlordId);
}
