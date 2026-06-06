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
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    // TODO: Get user by auth token (getMe)
    
    @GetMapping
    ResponseEntity<GenericResponse> getUserByEmail(@RequestParam(name = "email") String email) {
        UserResponse userFound = appUserService.getUserByEmail(email);
        return GenericResponse.builder()
                .message("User found")
                .data(userFound)
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }
}

