package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        String phone,
        UserRole role,
        LocalDateTime createdAt,
        Integer propertiesCount,
        Integer reservationsCount,
        Integer completedReservationsCount,
        Integer ratingsCount,
        Double averageScore,
        List<RatingResponse> ratings,
        String verificationStatus
) {}
