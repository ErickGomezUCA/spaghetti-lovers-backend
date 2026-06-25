package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.AvailabilityResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface AvailabilityService {
    AvailabilityResponse checkAvailability(UUID propertyId, LocalDate startDate, LocalDate endDate);
}
