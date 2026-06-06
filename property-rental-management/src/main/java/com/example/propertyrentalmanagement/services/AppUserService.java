package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.request.LoginRequest;
import com.example.propertyrentalmanagement.dto.response.UserResponse;

import java.util.UUID;

public interface AppUserService {
    UserResponse createUser(CreateUserRequest userRequest);

    UserResponse login(LoginRequest loginRequest);

    UserResponse getUserByEmail(String email);

    UserResponse getUserById(UUID userId);

    // TODO: If necessary, return value can be changed depending on rating response values
    UserResponse getUserRating(UUID userId);
}
