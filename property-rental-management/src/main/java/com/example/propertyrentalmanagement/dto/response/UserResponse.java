package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        String phone
) {
    public static UserResponse fromEntity(AppUser appUser) {
        return new UserResponse(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),
                appUser.getRole(),
                appUser.getPhone()
        );
    }

    public AppUser toEntity() {
        return AppUser.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .role(this.role)
                .phone(this.phone)
                .build();
    }
}

