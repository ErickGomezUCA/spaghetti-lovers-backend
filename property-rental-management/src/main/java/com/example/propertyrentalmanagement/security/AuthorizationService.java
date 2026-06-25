package com.example.propertyrentalmanagement.security;

import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final AuthenticatedUserProvider authenticatedUserProvider;

    public boolean isAdmin() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();
        return currentUser.getRole() == UserRole.ADMIN;
    }

    public boolean isLandlord() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();
        return currentUser.getRole() == UserRole.LANDLORD;
    }

    public boolean isTenant() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();
        return currentUser.getRole() == UserRole.TENANT;
    }

    public boolean isCurrentUser(UUID userId) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();
        return currentUser.getId().equals(userId);
    }

    public boolean isAdminOrCurrentUser(UUID userId) {
        return isAdmin() || isCurrentUser(userId);
    }
}
