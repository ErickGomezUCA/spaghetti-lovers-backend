package com.example.propertyrentalmanagement.dto.response;

import java.util.List;
import java.util.UUID;

public record UserRatingsResponse(
        UUID userId,
        Double averageScore,
        Integer totalRatings,
        List<RatingResponse> ratings
){
}
