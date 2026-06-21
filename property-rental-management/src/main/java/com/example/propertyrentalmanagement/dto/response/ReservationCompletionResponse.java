package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCompletionResponse(
        UUID reservationId,
        ReservationStatus reservationStatus,
        BigDecimal guaranteeDepositAmount,
        BigDecimal retainedAmount,
        BigDecimal guaranteeDepositRefundAmount,
        BigDecimal additionalFinePaymentAmount,
        LocalDateTime completedAt
) {
    public static ReservationCompletionResponse fromEntity(
            Reservation reservation,
            BigDecimal guaranteeDepositAmount,
            BigDecimal retainedAmount,
            BigDecimal guaranteeDepositRefundAmount,
            BigDecimal additionalFinePaymentAmount,
            LocalDateTime completedAt
    ) {
        return new ReservationCompletionResponse(
                reservation.getId(),
                reservation.getReservationStatus(),
                guaranteeDepositAmount,
                retainedAmount,
                guaranteeDepositRefundAmount,
                additionalFinePaymentAmount,
                completedAt
        );
    }
}