package com.example.propertyrentalmanagement.exceptions;

public class IdentityDocumentNotFoundException extends RuntimeException {
    public IdentityDocumentNotFoundException(String message) {
        super(message);
    }
}