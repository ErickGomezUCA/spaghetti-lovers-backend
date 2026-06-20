package com.example.propertyrentalmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$",
                message = "Password must be at least 12 characters long and include at least 1 uppercase letter, 1 number, and 1 symbol"
        )
        String newPassword
) {
}
