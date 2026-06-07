package com.example.propertyrentalmanagement.exceptions;

public class NotResourceOwnerException extends RuntimeException {
    public NotResourceOwnerException(String message) {
        super(message);
    }
}
