package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.AccessCode;
import com.example.propertyrentalmanagement.enums.AccessCodeStatus;
import com.example.propertyrentalmanagement.enums.CodeType;
import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccessCodeDetailResponse(
        UUID accessCodeId,
        UUID reservationId,
        UUID propertyId,
        String propertyTitle,
        String propertyCity,
        String propertyDepartment,
        UUID tenantId,
        String tenantName,
        String code,
        CodeType codeType,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        Boolean isActive,
        AccessCodeStatus accessCodeStatus,
        ReservationStatus reservationStatus
) {
    public static AccessCodeDetailResponse fromEntity(AccessCode accessCode) {
        return new AccessCodeDetailResponse(
                accessCode.getId(),
                accessCode.getReservation().getId(),
                accessCode.getProperty().getId(),
                accessCode.getProperty().getTitle(),
                accessCode.getProperty().getCity(),
                accessCode.getProperty().getDepartment(),
                accessCode.getReservation().getTenant().getId(),
                accessCode.getReservation().getTenant().getName(),
                accessCode.getCode(),
                accessCode.getCodeType(),
                accessCode.getReservation().getCheckInDate(),
                accessCode.getReservation().getCheckOutDate(),
                accessCode.getValidFrom(),
                accessCode.getValidUntil(),
                accessCode.getIsActive(),
                resolveStatus(accessCode),
                accessCode.getReservation().getReservationStatus()
        );
    }

    private static AccessCodeStatus resolveStatus(AccessCode accessCode) {
        if (!Boolean.TRUE.equals(accessCode.getIsActive())) {
            return AccessCodeStatus.INACTIVE;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(accessCode.getValidFrom())) {
            return AccessCodeStatus.PENDING;
        }

        if (now.isAfter(accessCode.getValidUntil())) {
            return AccessCodeStatus.EXPIRED;
        }

        return AccessCodeStatus.ACTIVE;
    }
}