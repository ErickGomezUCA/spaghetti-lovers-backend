package com.example.propertyrentalmanagement.exceptions;

public class ContractAlreadyExistsException extends RuntimeException {
    public ContractAlreadyExistsException(String message) {
        super(message);
    }
}
