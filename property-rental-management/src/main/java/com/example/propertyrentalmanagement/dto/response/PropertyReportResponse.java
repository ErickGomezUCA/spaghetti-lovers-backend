package com.example.propertyrentalmanagement.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertyReportResponse(
        UUID propertyId,
        PeriodResponse period,
        Double occupancyRate,
        Integer totalNightsOccupied,
        Integer totalReservations,
        RevenueResponse revenue
) {
    public record PeriodResponse(
            String start,
            String end
    ) {}

    public record RevenueResponse(
            BigDecimal base,
            BigDecimal cleaning,
            BigDecimal penalties,
            BigDecimal total
    ) {}
}