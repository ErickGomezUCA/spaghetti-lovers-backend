package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.enums.PropertyStatus;
import com.example.propertyrentalmanagement.exceptions.PropertyNotFound;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.services.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public PropertyResponse createProperty(CreatePropertyRequest propertyRequest, UUID landlordId) {
        AppUser landlord = appUserRepository.findById(landlordId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Property property = Property.builder()
                .landlord(landlord)
                .title(propertyRequest.getTitle())
                .description(propertyRequest.getDescription())
                .address(propertyRequest.getAddress())
                .city(propertyRequest.getCity())
                .department(propertyRequest.getDepartment())
                .country(propertyRequest.getCountry())
                .basePricePerNight(propertyRequest.getBasePricePerNight())
                .cleaningFee(propertyRequest.getCleaningFee())
                .securityDepositAmount(propertyRequest.getSecurityDepositAmount())
                .maxGuests(propertyRequest.getMaxGuests())
                .bedrooms(propertyRequest.getBedrooms())
                .bathrooms(propertyRequest.getBathrooms())
                .areaSqm(propertyRequest.getAreaSqm())
                .propertyType(propertyRequest.getPropertyType())
                .propertyStatus(PropertyStatus.ACTIVE)
                .rules(propertyRequest.getRules()).build();

        Property createdProperty = propertyRepository.save(property);
        return PropertyResponse.fromEntity(createdProperty);
    }

    @Override
    public PropertyResponse getPropertyById(UUID propertyId) {
        Property propertyFound = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));
        return PropertyResponse.fromEntity(propertyFound);
    }

    @Override
    public List<PropertyResponse> getAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        return properties.stream().map(PropertyResponse::fromEntity).toList();
    }

    @Override
    public List<PropertyResponse> getPropertiesByLandlordId(UUID landlordId) {
        List<Property> properties = propertyRepository.findByLandlordId(landlordId);
        return properties.stream().map(PropertyResponse::fromEntity).toList();
    }
}
