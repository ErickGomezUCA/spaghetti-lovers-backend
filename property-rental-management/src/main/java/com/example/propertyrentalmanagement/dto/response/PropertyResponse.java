package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.entitites.PropertyPhoto;
import com.example.propertyrentalmanagement.enums.PropertyStatus;
import com.example.propertyrentalmanagement.enums.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PropertyResponse(
        UUID id,
        UUID landlordId,
        String title,
        String description,
        String address,
        String city,
        String department,
        String country,
        BigDecimal basePricePerNight,
        BigDecimal cleaningFee,
        BigDecimal securityDepositAmount,
        Integer maxGuests,
        Integer bedrooms,
        Integer bathrooms,
        BigDecimal areaSqm,
        PropertyType propertyType,
        PropertyStatus propertyStatus,
        String rules,
        List<String> photoUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PropertyResponse fromEntity(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getLandlord() != null ? property.getLandlord().getId() : null,
                property.getTitle(),
                property.getDescription(),
                property.getAddress(),
                property.getCity(),
                property.getDepartment(),
                property.getCountry(),
                property.getBasePricePerNight(),
                property.getCleaningFee(),
                property.getSecurityDepositAmount(),
                property.getMaxGuests(),
                property.getBedrooms(),
                property.getBathrooms(),
                property.getAreaSqm(),
                property.getPropertyType(),
                property.getPropertyStatus(),
                property.getRules(),
                property.getPhotos() != null ? property.getPhotos().stream().map(PropertyPhoto::getUrl).toList() : List.of(),
                property.getCreatedAt(),
                property.getUpdatedAt()
        );
    }
}
