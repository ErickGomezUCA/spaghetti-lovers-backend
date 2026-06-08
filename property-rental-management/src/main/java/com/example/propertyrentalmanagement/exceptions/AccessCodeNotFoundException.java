package com.example.propertyrentalmanagement.exceptions;

public class AccessCodeNotFoundException extends RuntimeException {
    public AccessCodeNotFoundException(String message) {
        super(message);
    }
}

