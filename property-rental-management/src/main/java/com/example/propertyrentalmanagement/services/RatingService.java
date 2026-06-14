package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateRatingRequest;
import com.example.propertyrentalmanagement.dto.response.RatingResponse;
import com.example.propertyrentalmanagement.dto.response.UserRatingsResponse;

import java.util.UUID;

public interface RatingService {
    RatingResponse createRating(CreateRatingRequest request);

    UserRatingsResponse getRatingsByUser(UUID userId);
}
