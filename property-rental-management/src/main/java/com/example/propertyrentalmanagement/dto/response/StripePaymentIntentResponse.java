package com.example.propertyrentalmanagement.dto.response;

import java.math.BigDecimal;

public record StripePaymentIntentResponse(
        String clientSecret,
        String paymentIntentId,
        BigDecimal amount,
        String currency
) {}
