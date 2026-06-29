package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.AvailabilityCalendar;
import com.example.propertyrentalmanagement.enums.BlockType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse (
        boolean available,
        List<ConflictResponse> conflicts
) {
    public record ConflictResponse(
            UUID id,
            UUID propertyId,
            BlockType blockType,
            LocalDateTime timestampStart,
            LocalDateTime timestampEnd,
            String blockedReason
    ) {
        public static ConflictResponse fromEntity(AvailabilityCalendar calendar) {
            return new ConflictResponse(
                    calendar.getId(),
                    calendar.getProperty().getId(),
                    calendar.getBlockType(),
                    calendar.getTimestampStart(),
                    calendar.getTimestampEnd(),
                    calendar.getBlockedReason()
            );
        }
    }
}
