package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        UserRole role,
        String phone
) {
}

