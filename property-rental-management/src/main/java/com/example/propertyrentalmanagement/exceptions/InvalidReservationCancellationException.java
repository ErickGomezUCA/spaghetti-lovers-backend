package com.example.propertyrentalmanagement.exceptions;

public class InvalidReservationCancellationException extends RuntimeException {
    public InvalidReservationCancellationException(String message) {
        super(message);
    }
}
