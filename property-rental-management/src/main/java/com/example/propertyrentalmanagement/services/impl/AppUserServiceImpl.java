package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.request.LoginRequest;
import com.example.propertyrentalmanagement.dto.response.AuthResponse;
import com.example.propertyrentalmanagement.dto.response.UserRatingsResponse;
import com.example.propertyrentalmanagement.dto.response.UserResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.InvalidCredentials;
import com.example.propertyrentalmanagement.exceptions.UserAlreadyExistsException;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.security.JwtService;
import com.example.propertyrentalmanagement.services.AppUserService;
import com.example.propertyrentalmanagement.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RatingService ratingService;

    @Override
    public AuthResponse createUser(CreateUserRequest userRequest) {
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
        String token = jwtService.generateToken(createdUser);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(createdUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        AppUser userFound = appUserRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new InvalidCredentials("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), userFound.getPasswordHash())) {
            throw new InvalidCredentials("Invalid email or password");
        }

        String token = jwtService.generateToken(userFound);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(userFound))
                .build();
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        AppUser userFound = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserResponse.fromEntity(userFound);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        AppUser userFound = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserResponse.fromEntity(userFound);
    }

    // TODO: Pending to be implemented on: [SPL-22] Obtener Calificaciones de un Usuario
    @Override
    public UserRatingsResponse getUserRating(UUID userId) {
        return ratingService.getRatingsByUser(userId);
    }


}
