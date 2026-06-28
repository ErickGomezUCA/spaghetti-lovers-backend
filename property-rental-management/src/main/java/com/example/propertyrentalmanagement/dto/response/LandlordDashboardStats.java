package com.example.propertyrentalmanagement.dto.response;

import java.math.BigDecimal;

public record LandlordDashboardStats(
        BigDecimal monthlyIncome,
        double averageOccupation
) {}
