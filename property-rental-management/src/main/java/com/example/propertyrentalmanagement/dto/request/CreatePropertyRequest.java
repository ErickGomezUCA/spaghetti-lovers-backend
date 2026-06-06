package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotNull
    private String department;

    @NotNull
    private String country;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal basePricePerNight;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal cleaningFee;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal securityDepositAmount;

    @NotNull
    @Min(1)
    private Integer maxGuests;

    @NotNull
    @Min(0)
    private Integer bedrooms;

    @NotNull
    @Min(0)
    private Integer bathrooms;

    @NotNull
    @Min(0)
    private BigDecimal areaSqm;

    @NotNull
    private PropertyType propertyType;

    private String rules;
}
