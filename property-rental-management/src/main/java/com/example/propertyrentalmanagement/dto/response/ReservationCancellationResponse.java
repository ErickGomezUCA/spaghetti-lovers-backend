package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCancellationResponse(
        UUID reservationId,
        ReservationStatus reservationStatus,
        BigDecimal cancellationPenalty,
        BigDecimal reservationRefundAmount,
        BigDecimal cleaningFeeRefundAmount,
        BigDecimal guaranteeDepositRefundAmount,
        BigDecimal totalRefundAmount,
        LocalDateTime cancelledAt
) {
    public static ReservationCancellationResponse fromEntity(
            Reservation reservation,
            BigDecimal reservationRefundAmount,
            BigDecimal cleaningFeeRefundAmount,
            BigDecimal guaranteeDepositRefundAmount,
            BigDecimal totalRefundAmount
    ) {
        return new ReservationCancellationResponse(
                reservation.getId(),
                reservation.getReservationStatus(),
                reservation.getCancellationPenalty(),
                reservationRefundAmount,
                cleaningFeeRefundAmount,
                guaranteeDepositRefundAmount,
                totalRefundAmount,
                reservation.getCancelledAt()
        );
    }
}