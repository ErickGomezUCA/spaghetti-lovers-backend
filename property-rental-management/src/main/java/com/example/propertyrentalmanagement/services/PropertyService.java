package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.AttachPhotoRequest;
import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.request.UpdatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PropertyService {
    PropertyResponse createProperty(CreatePropertyRequest propertyRequest);

    PropertyResponse attachPhotosToProperty(UUID propertyId, AttachPhotoRequest photoUrls);

    PropertyResponse getPropertyById(UUID propertyId);

    Page<PropertyResponse> getAllProperties(int page, int pageSize, String sortBy, String sortOrder);

    Page<PropertyResponse> getPropertiesByLandlordId(UUID landlordId, int page, int pageSize, String sortBy, String sortOrder);

    PropertyResponse updateProperty(UUID propertyId, UpdatePropertyRequest propertyRequest);

    void deleteProperty(UUID propertyId);
}
