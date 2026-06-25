package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.AccessCode;
import com.example.propertyrentalmanagement.enums.CodeType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccessCodeResponse(
        UUID id,
        UUID propertyId,
        UUID reservationId,
        String code,
        CodeType codeType,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        Boolean isActive
) {
    public static AccessCodeResponse fromEntity(AccessCode accessCode) {
        return new AccessCodeResponse(
                accessCode.getId(),
                accessCode.getProperty().getId(),
                accessCode.getReservation() != null ? accessCode.getReservation().getId() : null,
                accessCode.getCode(),
                accessCode.getCodeType(),
                accessCode.getValidFrom(),
                accessCode.getValidUntil(),
                accessCode.getIsActive()
        );
    }
}
