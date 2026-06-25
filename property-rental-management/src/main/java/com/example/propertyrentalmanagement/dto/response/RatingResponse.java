package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Rating;

import java.time.LocalDateTime;
import java.util.UUID;

public record RatingResponse (
        UUID id,
        UUID reservationId,
        UUID reviewerId,
        UUID reviewedId,
        Integer score,
        String comment,
        LocalDateTime createdAt
){
    public static RatingResponse fromEntity(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getReservation().getId(),
                rating.getReviewer().getId(),
                rating.getReviewed().getId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }
}