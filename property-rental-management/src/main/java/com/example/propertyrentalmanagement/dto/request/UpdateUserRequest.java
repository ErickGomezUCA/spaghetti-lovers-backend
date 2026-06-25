package com.example.propertyrentalmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String phone
) {
}
