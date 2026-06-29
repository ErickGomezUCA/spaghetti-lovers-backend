package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Fine;
import com.example.propertyrentalmanagement.enums.FineType;
import com.example.propertyrentalmanagement.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FineSummaryResponse(
        UUID fineId,
        UUID reservationId,
        String propertyName,
        String tenantName,
        String tenantEmail,
        FineType fineType,
        String description,
        BigDecimal amount,
        LocalDateTime issuedAt,
        LocalDateTime resolvedAt,
        PaymentMethod paymentMethod
) {
    public static FineSummaryResponse fromEntity(Fine fine) {
        return new FineSummaryResponse(
                fine.getId(),
                fine.getReservation().getId(),
                fine.getReservation().getProperty().getTitle(),
                fine.getReservation().getTenant().getName(),
                fine.getReservation().getTenant().getEmail(),
                fine.getFineType(),
                fine.getDescription(),
                fine.getAmount(),
                fine.getIssuedAt(),
                fine.getResolvedAt(),
                fine.getPayment().getPaymentMethod()
        );
    }
}