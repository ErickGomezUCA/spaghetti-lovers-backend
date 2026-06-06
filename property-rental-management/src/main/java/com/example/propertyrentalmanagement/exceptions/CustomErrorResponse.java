package com.example.propertyrentalmanagement.exceptions;

import java.time.LocalDateTime;

public record CustomErrorResponse(
        LocalDateTime timestamp,
        String message
) {
}
