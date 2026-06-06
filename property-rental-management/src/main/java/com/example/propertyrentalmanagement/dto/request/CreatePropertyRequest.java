package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreatePropertyRequest(
        @NotBlank
        String title,

        String description,

        @NotBlank
        String address,

        @NotBlank
        String city,

        @NotNull
        String department,

        @NotNull
        String country,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal basePricePerNight,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal cleaningFee,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal securityDepositAmount,

        @NotNull
        @Min(1)
        Integer maxGuests,

        @NotNull
        @Min(0)
        Integer bedrooms,

        @NotNull
        @Min(0)
        Integer bathrooms,

        @NotNull
        @Min(0)
        BigDecimal areaSqm,

        @NotNull
        PropertyType propertyType,

        String rules,

//        Optionals
        List<String> photoUrls
) {
}
