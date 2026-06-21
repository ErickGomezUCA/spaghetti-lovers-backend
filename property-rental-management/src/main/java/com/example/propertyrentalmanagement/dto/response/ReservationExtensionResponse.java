package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Payment;
import com.example.propertyrentalmanagement.entitites.Reservation;

public record ReservationExtensionResponse(
        ReservationResponse reservation,
        PaymentResponse extensionPayment
) {
    public static ReservationExtensionResponse fromEntity(Reservation reservation, Payment extensionPayment) {
        return new ReservationExtensionResponse(
                ReservationResponse.fromEntity(reservation),
                PaymentResponse.fromEntity(extensionPayment)
        );
    }
}