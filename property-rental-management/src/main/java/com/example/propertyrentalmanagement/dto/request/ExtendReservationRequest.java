package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ExtendReservationRequest(
        @NotNull(message = "New check-out date is required")
        @Future(message = "New check-out date must be in the future")
        LocalDate newCheckOutDate,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}