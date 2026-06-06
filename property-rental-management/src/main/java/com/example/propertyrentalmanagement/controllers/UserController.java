package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.UserResponse;
import com.example.propertyrentalmanagement.services.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserService appUserService;

    @PostMapping
    ResponseEntity<GenericResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse createdUser = appUserService.createUser(request);

        return GenericResponse.builder()
                .message("User created successfully")
                .data(createdUser)
                .resourceId(createdUser.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    // TODO: Get user by auth token (getMe) on: [SPL-31] Authentication y Authorization, incluyendo Roles

    @GetMapping
    ResponseEntity<GenericResponse> getUserByEmail(@RequestParam(name = "email") String email) {
        UserResponse userFound = appUserService.getUserByEmail(email);
        return GenericResponse.builder()
                .message("User found")
                .data(userFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/{userId}")
    ResponseEntity<GenericResponse> getUserByUUID(@PathVariable UUID userId) {
        UserResponse userFound = appUserService.getUserById(userId);
        return GenericResponse.builder()
                .message("User found")
                .data(userFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

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
}
