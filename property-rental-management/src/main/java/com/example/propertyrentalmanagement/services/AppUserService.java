package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.ChangePasswordRequest;
import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.request.LoginRequest;
import com.example.propertyrentalmanagement.dto.request.UpdateUserRequest;
import com.example.propertyrentalmanagement.dto.response.AuthResponse;
import com.example.propertyrentalmanagement.dto.response.UserRatingsResponse;
import com.example.propertyrentalmanagement.dto.response.UserResponse;

import java.util.UUID;

public interface AppUserService {
    AuthResponse createUser(CreateUserRequest userRequest);

    AuthResponse login(LoginRequest loginRequest);

    UserResponse getUserByEmail(String email);

    UserResponse getUserById(UUID userId);

    // TODO: If necessary, return value can be changed depending on rating response values
    UserRatingsResponse getUserRating(UUID userId);

    UserResponse updateUser(UUID userId, UpdateUserRequest updateUserRequest);

    void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest);
}
