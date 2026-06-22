package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.Contract;
import com.example.propertyrentalmanagement.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID reservationId,
        String contentUrl,
        ContractStatus contractStatus,
        UUID tenantSignatureId,
        UUID landlordSignatureId,
        String createdAtTimestamp,
        String expiresAtTimestamp,
        String propertyTitle,
        String propertyCity,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        String tenantName,
        String landlordName
) {
    public static ContractResponse fromEntity(Contract contract) {
        var reservation = contract.getReservation();
        var property = reservation.getProperty();

        return new ContractResponse(
                contract.getId(),
                reservation.getId(),
                contract.getContentUrl(),
                contract.getContractStatus(),
                contract.getTenantSignature() != null ? contract.getTenantSignature().getId() : null,
                contract.getLandlordSignature() != null ? contract.getLandlordSignature().getId() : null,
                contract.getCreatedAtTimestamp() != null ? contract.getCreatedAtTimestamp().toString() : null,
                contract.getExpiresAtTimestamp() != null ? contract.getExpiresAtTimestamp().toString() : null,
                property.getTitle(),
                property.getCity(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getTotalPrice(),
                reservation.getTenant().getName(),
                property.getLandlord().getName()
        );
    }
}
