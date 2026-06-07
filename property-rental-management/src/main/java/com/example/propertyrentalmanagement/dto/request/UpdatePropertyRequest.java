package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.PropertyStatus;
import com.example.propertyrentalmanagement.enums.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdatePropertyRequest(
        String title,

        String description,

        String address,

        String city,

        String department,

        String country,

        @DecimalMin("0.01")
        BigDecimal basePricePerNight,

        @DecimalMin("0.00")
        BigDecimal cleaningFee,

        @DecimalMin("0.00")
        BigDecimal securityDepositAmount,

        @Min(1)
        Integer maxGuests,

        @Min(0)
        Integer bedrooms,

        @Min(0)
        Integer bathrooms,

        @Min(0)
        BigDecimal areaSqm,

        PropertyType propertyType,

        PropertyStatus propertyStatus,

        String rules
) {
}

