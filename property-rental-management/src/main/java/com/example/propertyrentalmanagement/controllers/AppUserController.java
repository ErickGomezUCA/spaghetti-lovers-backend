package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.request.LoginRequest;
import com.example.propertyrentalmanagement.dto.response.AuthResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.UserResponse;
import com.example.propertyrentalmanagement.services.AppUserService;
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

    // TODO: Include token in response: [SPL-31] Authentication y Authorization, incluyendo Roles
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

    // TODO: Include token in response: [SPL-31] Authentication y Authorization, incluyendo Roles
    @PostMapping("/login")
    ResponseEntity<GenericResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = appUserService.login(request);

        return GenericResponse.builder()
                .message("Login successful")
                .data(authResponse)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    // TODO: Get user by auth token (getMe) on: [SPL-31] Authentication y Authorization, incluyendo Roles


    // TODO: Pending to be implemented on: [SPL-22] Obtener Calificaciones de un Usuario
    @GetMapping("/{userId}/rating")
    ResponseEntity<GenericResponse> getUserRating(@PathVariable UUID userId) {
        UserResponse userFound = appUserService.getUserRating(userId);
        return GenericResponse.builder()
                .message("User found")
                .data(userFound)
                .status(HttpStatus.OK)
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
