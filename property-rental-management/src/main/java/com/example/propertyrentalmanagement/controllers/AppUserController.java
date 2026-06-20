package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.*;
import com.example.propertyrentalmanagement.dto.response.*;
import com.example.propertyrentalmanagement.services.AppUserService;
import com.example.propertyrentalmanagement.services.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final RatingService ratingService;

    @PostMapping("/register")
    ResponseEntity<GenericResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        AuthResponse authResponse = appUserService.createUser(request);

        return GenericResponse.builder()
                .message("User created successfully")
                .data(authResponse)
                .resourceId(authResponse.user().id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @PostMapping("/login")
    ResponseEntity<GenericResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = appUserService.login(request);

        return GenericResponse.builder()
                .message("Login successful")
                .data(authResponse)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    // BUG: Updating email makes getName() to return null
    @PutMapping("/update")
    ResponseEntity<GenericResponse> updateUser(Authentication authentication, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        UserResponse authUserResponse = appUserService.getUserByEmail(authentication.getName());
        UserResponse userResponse = appUserService.updateUser(authUserResponse.id(), updateUserRequest);

        return GenericResponse.builder()
                .message("User updated successfully")
                .data(userResponse)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PostMapping("/change-password")
    ResponseEntity<GenericResponse> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        UserResponse authUserResponse = appUserService.getUserByEmail(authentication.getName());

        appUserService.changePassword(authUserResponse.id(), changePasswordRequest);

        return GenericResponse.builder()
                .message("Password updated successfully")
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    // TODO: Pending to be implemented on: [SPL-22] Obtener Calificaciones de un Usuario
    @GetMapping("/{userId}/rating")
    ResponseEntity<GenericResponse> getUserRating(@PathVariable UUID userId) {
        UserRatingsResponse ratingsFound = appUserService.getUserRating(userId);
        return GenericResponse.builder()
                .message("User ratings found")
                .data(ratingsFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PostMapping("/ratings")
    ResponseEntity<GenericResponse> createRating(@Valid @RequestBody CreateRatingRequest request) {
        RatingResponse created = ratingService.createRating(request);
        return GenericResponse.builder()
                .message("Rating created successfully")
                .data(created)
                .resourceId(created.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @GetMapping("/me")
    ResponseEntity<GenericResponse> getAuthenticatedUser(Authentication authentication) {
        UserResponse userFound = appUserService.getUserByEmail(authentication.getName());

        return GenericResponse.builder()
                .message("Authenticated user found")
                .data(userFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isAdmin()")
    @GetMapping
    ResponseEntity<GenericResponse> getUserByEmail(@RequestParam(name = "email") String email) {
        UserResponse userFound = appUserService.getUserByEmail(email);

        return GenericResponse.builder()
                .message("User found")
                .data(userFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isAdminOrCurrentUser(#userId)")
    @GetMapping("/{userId}")
    ResponseEntity<GenericResponse> getUserByUUID(@PathVariable UUID userId) {
        UserResponse userFound = appUserService.getUserById(userId);

        return GenericResponse.builder()
                .message("User found")
                .data(userFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
