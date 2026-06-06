package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.response.UserResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.UserAlreadyExistsException;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest userRequest) {
        if (appUserRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        UserRole role = userRequest.role() == null ? UserRole.TENANT : userRequest.role();
        AppUser user = AppUser.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .passwordHash(passwordEncoder.encode(userRequest.password()))
                .role(role)
                .phone(userRequest.phone())
                .build();

        AppUser createdUser = appUserRepository.save(user);
        return UserResponse.fromEntity(createdUser);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        AppUser userFound = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserResponse.fromEntity(userFound);
    }
}

