package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Contract;
import com.example.propertyrentalmanagement.enums.ContractStatus;

import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID reservationId,
        String contentUrl,
        ContractStatus contractStatus,
        UUID tenantSignatureId,
        UUID landlordSignatureId,
        String createdAtTimestamp,
        String expiresAtTimestamp
) {
    public static ContractResponse fromEntity(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getReservation().getId(),
                contract.getContentUrl(),
                contract.getContractStatus(),
                contract.getTenantSignature() != null ? contract.getTenantSignature().getId() : null,
                contract.getLandlordSignature() != null ? contract.getLandlordSignature().getId() : null,
                contract.getCreatedAtTimestamp() != null ? contract.getCreatedAtTimestamp().toString() : null,
                contract.getExpiresAtTimestamp() != null ? contract.getExpiresAtTimestamp().toString() : null
        );
    }
}
