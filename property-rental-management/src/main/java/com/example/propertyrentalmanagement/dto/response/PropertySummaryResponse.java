package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Property;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertySummaryResponse(
        UUID id,
        String title,
        String address,
        String city,
        String department,
        BigDecimal basePricePerNight,
        BigDecimal securityDepositAmount,
        String mainPhotoUrl
) {
    public static PropertySummaryResponse fromEntity(Property property) {
        String mainPhoto = (property.getPhotos() != null && !property.getPhotos().isEmpty())
                ? property.getPhotos().get(0).getUrl()
                : null;

        return new PropertySummaryResponse(
                property.getId(),
                property.getTitle(),
                property.getAddress(),
                property.getCity(),
                property.getDepartment(),
                property.getBasePricePerNight(),
                property.getSecurityDepositAmount(),
                mainPhoto
        );
    }
}