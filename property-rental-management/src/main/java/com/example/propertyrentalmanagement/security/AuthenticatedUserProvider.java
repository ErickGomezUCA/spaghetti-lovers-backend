package com.example.propertyrentalmanagement.security;

import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {

    private final AppUserRepository appUserRepository;

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("Authenticated user not found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return appUserRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
        }

        throw new UserNotFoundException("Authenticated user not found");
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}