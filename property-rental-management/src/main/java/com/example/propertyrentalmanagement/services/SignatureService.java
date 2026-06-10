package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.entitites.Signature;

import java.util.UUID;

public interface SignatureService {
    Signature createSignature(UUID userId, UUID contractId);
}
