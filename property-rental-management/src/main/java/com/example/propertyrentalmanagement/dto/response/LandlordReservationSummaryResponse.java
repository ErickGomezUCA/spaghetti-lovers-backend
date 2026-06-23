package com.example.propertyrentalmanagement.dto.response;

public record LandlordReservationSummaryResponse(
        long reserved,
        long active,
        long completed,
        long cancelled
) {

}