package com.example.propertyrentalmanagement.dto.request;

import java.util.UUID;

public record CreateContractRequest(
        UUID reservationId
) {
}
