package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.enums.ReservationStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReservationCompletionResponse(
        UUID reservationId,
        ReservationStatus reservationStatus,
        BigDecimal guaranteeDepositAmount,
        BigDecimal retainedAmount,
        BigDecimal guaranteeDepositRefundAmount,
        BigDecimal additionalFinePaymentAmount,
        LocalDateTime completedAt
) {
}
