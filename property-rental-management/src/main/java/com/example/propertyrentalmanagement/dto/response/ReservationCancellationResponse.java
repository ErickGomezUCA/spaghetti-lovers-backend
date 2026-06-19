package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.enums.ReservationStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
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
}
