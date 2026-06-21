package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.PaymentMethod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateReservationRequest(
        @NotNull
        UUID propertyId,

        @NotNull
        @FutureOrPresent(message = "Check-in date must be a date in the present or in the future")
        LocalDate checkInDate,

        @NotNull
        LocalDate checkOutDate,

        @Min(1)
        Integer guestsCount,

        @NotNull
        PaymentMethod paymentMethod
) {}