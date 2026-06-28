package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.ChangePasswordRequest;
import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.request.LoginRequest;
import com.example.propertyrentalmanagement.dto.request.UpdateUserRequest;
import com.example.propertyrentalmanagement.dto.response.AdminMonthlySummary;
import com.example.propertyrentalmanagement.dto.response.AuthResponse;
import com.example.propertyrentalmanagement.dto.response.UserProfileResponse;
import com.example.propertyrentalmanagement.dto.response.UserRatingsResponse;
import com.example.propertyrentalmanagement.dto.response.UserResponse;
import com.example.propertyrentalmanagement.enums.UserRole;
import org.springframework.data.domain.Page;
import com.example.propertyrentalmanagement.enums.UserRole;

import java.util.List;
import java.util.UUID;

public interface AppUserService {
    AuthResponse createUser(CreateUserRequest userRequest);

    AuthResponse login(LoginRequest loginRequest);

    UserResponse getUserByEmail(String email);

    UserResponse getUserById(UUID userId);

    // TODO: If necessary, return value can be changed depending on rating response values
    UserRatingsResponse getUserRating(UUID userId);

    UserProfileResponse getUserProfile(String email);

    Page<UserProfileResponse> getAllUsersForAdmin(int page, int pageSize, String sortBy, String sortOrder, UserRole role, String search);

    UserProfileResponse getUserProfileById(UUID userId);

    UserResponse updateUser(UUID userId, UpdateUserRequest updateUserRequest);

    void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest);

    List<UserResponse> getUsersByRole(UserRole role);

    AdminMonthlySummary getAdminMonthlySummary(long activePropertiesCount);
}
