package com.example.propertyrentalmanagement.dto.request;

import com.example.propertyrentalmanagement.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$",
                message = "Password must be at least 12 characters long and include at least 1 uppercase letter, 1 number, and 1 symbol"
        )
        String password,
        UserRole role,
        String phone
) {
}

