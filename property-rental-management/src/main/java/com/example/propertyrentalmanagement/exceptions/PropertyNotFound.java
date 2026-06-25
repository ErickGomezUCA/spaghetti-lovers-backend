package com.example.propertyrentalmanagement.exceptions;

public class PropertyNotFound extends RuntimeException {
    public PropertyNotFound(String message) {
        super(message);
    }
}
