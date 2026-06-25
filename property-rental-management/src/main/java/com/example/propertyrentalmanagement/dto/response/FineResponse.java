package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Fine;
import com.example.propertyrentalmanagement.entitites.Payment;
import com.example.propertyrentalmanagement.enums.FineType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FineResponse(
        UUID fineId,
        UUID reservationId,
        UUID paymentId,
        FineType fineType,
        String description,
        BigDecimal amount,
        LocalDateTime issuedAt,
        LocalDateTime resolvedAt
) {
    public static FineResponse fromEntity(Fine fine, Payment payment) {
        return new FineResponse(
                fine.getId(),
                fine.getReservation().getId(),
                payment.getId(),
                fine.getFineType(),
                fine.getDescription(),
                fine.getAmount(),
                fine.getIssuedAt(),
                fine.getResolvedAt()
        );
    }
}