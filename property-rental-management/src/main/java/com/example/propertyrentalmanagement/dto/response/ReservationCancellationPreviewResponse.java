package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReservationCancellationPreviewResponse(
        UUID reservationId,
        ReservationStatus reservationStatus,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long daysUntilCheckIn,
        BigDecimal cancellationPenalty,
        BigDecimal reservationRefundAmount,
        BigDecimal cleaningFeeRefundAmount,
        BigDecimal guaranteeDepositRefundAmount,
        BigDecimal totalRefundAmount
) {
}

