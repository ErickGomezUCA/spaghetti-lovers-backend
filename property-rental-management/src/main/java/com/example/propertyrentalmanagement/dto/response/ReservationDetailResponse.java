package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReservationDetailResponse(
        UUID id,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int guestsCount,
        int totalNights,
        BigDecimal baseTotal,
        BigDecimal cleaningFee,
        BigDecimal securityDepositAmount,
        BigDecimal longStayDiscount,
        BigDecimal totalPrice,
        ReservationStatus reservationStatus,
        LocalDateTime createdAt,
        String accessCode,
        PropertySummaryResponse property,
        UUID tenantId,
        String tenantName,
        String tenantEmail,
        List<PaymentResponse> payments,
        ContractResponse contract
) {
    public static ReservationDetailResponse fromEntity(
            Reservation reservation,
            PropertySummaryResponse property,
            String accessCode,
            List<PaymentResponse> payments,
            ContractResponse contract
    ) {
        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getGuestsCount(),
                reservation.getTotalNights(),
                reservation.getBaseTotal(),
                reservation.getCleaningFee(),
                reservation.getProperty().getSecurityDepositAmount(),
                reservation.getLongStayDiscount(),
                reservation.getTotalPrice(),
                reservation.getReservationStatus(),
                reservation.getCreatedAt(),
                accessCode,
                property,
                reservation.getTenant().getId(),
                reservation.getTenant().getName(),
                reservation.getTenant().getEmail(),
                payments,
                contract
        );
    }
}