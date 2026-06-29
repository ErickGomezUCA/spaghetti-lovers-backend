package com.example.propertyrentalmanagement.dto.response;

import java.math.BigDecimal;

public record AdminMonthlySummary(
        long reservationsThisMonth,
        BigDecimal incomeThisMonth,
        double averageOccupation
) {}
