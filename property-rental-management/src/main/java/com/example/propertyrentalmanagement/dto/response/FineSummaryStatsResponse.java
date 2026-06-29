package com.example.propertyrentalmanagement.dto.response;

import java.math.BigDecimal;

public record FineSummaryStatsResponse(
        long totalFines,
        long pendingCount,
        BigDecimal pendingAmount,
        BigDecimal resolvedAmount
) {}