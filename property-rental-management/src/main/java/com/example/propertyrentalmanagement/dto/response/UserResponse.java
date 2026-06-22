package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        String phone,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(AppUser appUser) {
        return new UserResponse(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),
                appUser.getRole(),
                appUser.getPhone(),
                appUser.getCreatedAt()
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

