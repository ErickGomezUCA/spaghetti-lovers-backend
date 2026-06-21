package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Payment;
import com.example.propertyrentalmanagement.enums.PaymentMethod;
import com.example.propertyrentalmanagement.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID reservationId,
        BigDecimal amount,
        PaymentType paymentType,
        PaymentMethod paymentMethod,
        BigDecimal refundAmount,
        LocalDateTime refundedAt,
        LocalDateTime createdAt
) {
    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getAmount(),
                payment.getPaymentType(),
                payment.getPaymentMethod(),
                payment.getRefundAmount(),
                payment.getRefundedAt(),
                payment.getCreatedAt()
        );
    }
}