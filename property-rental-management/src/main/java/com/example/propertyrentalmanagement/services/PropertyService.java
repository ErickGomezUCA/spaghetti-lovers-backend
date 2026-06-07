package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.AttachPhotoRequest;
import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.request.UpdatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;

import java.util.List;
import java.util.UUID;

public interface PropertyService {
    PropertyResponse createProperty(CreatePropertyRequest propertyRequest, UUID landlordId);

    PropertyResponse attachPhotosToProperty(UUID propertyId, AttachPhotoRequest photoUrls);

    PropertyResponse getPropertyById(UUID propertyId);

    List<PropertyResponse> getAllProperties();

    List<PropertyResponse> getPropertiesByLandlordId(UUID landlordId);

    PropertyResponse updateProperty(UUID landlordId, UUID propertyId, UpdatePropertyRequest propertyRequest);
}
