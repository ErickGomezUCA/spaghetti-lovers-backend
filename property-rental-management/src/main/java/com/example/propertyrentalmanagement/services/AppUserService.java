package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.response.UserResponse;

public interface AppUserService {
    UserResponse createUser(CreateUserRequest userRequest);

    UserResponse getUserByEmail(String email);
}

