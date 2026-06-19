package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.response.AvailabilityResponse;
import com.example.propertyrentalmanagement.entitites.AvailabilityCalendar;
import com.example.propertyrentalmanagement.exceptions.PropertyNotFound;
import com.example.propertyrentalmanagement.repositories.AvailabilityCalendarRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.services.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityCalendarRepository availabilityCalendarRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public AvailabilityResponse checkAvailability(UUID propertyId, LocalDate startDate, LocalDate endDate) {

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        List<AvailabilityCalendar> overlaps = availabilityCalendarRepository.findOverlappingBlocks(
                propertyId,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );

        boolean available = overlaps.isEmpty();

        List<AvailabilityResponse.ConflictResponse> conflicts = overlaps.stream()
                .map(AvailabilityResponse.ConflictResponse::fromEntity)
                .toList();

        return new AvailabilityResponse(available, conflicts);
    }
}
