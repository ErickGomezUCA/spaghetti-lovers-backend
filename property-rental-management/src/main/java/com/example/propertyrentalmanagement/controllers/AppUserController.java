package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.*;
import com.example.propertyrentalmanagement.dto.response.*;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.AppUserService;
import com.example.propertyrentalmanagement.services.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final RatingService ratingService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

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
    ResponseEntity<GenericResponse> updateUser(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        UserResponse userResponse = appUserService.updateUser(currentUser.getId(), updateUserRequest);

        return GenericResponse.builder()
                .message("User updated successfully")
                .data(userResponse)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PostMapping("/change-password")
    ResponseEntity<GenericResponse> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        appUserService.changePassword(currentUser.getId(), changePasswordRequest);

        return GenericResponse.builder()
                .message("Password updated successfully")
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

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
    ResponseEntity<GenericResponse> getAuthenticatedUser() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        UserResponse userFound = appUserService.getUserById(currentUser.getId());

        return GenericResponse.builder()
                .message("Authenticated user found")
                .data(userFound)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/profile")
    ResponseEntity<GenericResponse> getAuthenticatedUserProfile(Authentication authentication) {
        UserProfileResponse profile = appUserService.getUserProfile(authentication.getName());

        return GenericResponse.builder()
                .message("User profile found")
                .data(profile)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PreAuthorize("@authorizationService.isAdmin()")
    @GetMapping("/all")
    ResponseEntity<GenericResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String search
    ) {
        Page<UserProfileResponse> users = appUserService.getAllUsersForAdmin(page, pageSize, sortBy, sortOrder, role, search);

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .message("Users found")
                        .data(users.getContent())
                        .pagination(PaginationMeta.fromPage(users))
                        .status(HttpStatus.OK)
                        .build()
        );
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

    @PreAuthorize("@authorizationService.isAdmin()")
    @GetMapping("/{userId}/profile")
    ResponseEntity<GenericResponse> getUserProfileById(@PathVariable UUID userId) {
        UserProfileResponse profile = appUserService.getUserProfileById(userId);

        return GenericResponse.builder()
                .message("User profile found")
                .data(profile)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @GetMapping("/{userId}/ratings-given")
    ResponseEntity<GenericResponse> getRatingsGivenByUser(@PathVariable UUID userId) {
        List<RatingResponse> ratingsGiven = ratingService.getRatingsByReviewer(userId);
        return GenericResponse.builder()
                .message("Ratings given by user found")
                .data(ratingsGiven)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
