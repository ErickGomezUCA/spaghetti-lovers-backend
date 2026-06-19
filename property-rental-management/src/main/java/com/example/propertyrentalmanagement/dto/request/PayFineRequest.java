package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PayFineRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}