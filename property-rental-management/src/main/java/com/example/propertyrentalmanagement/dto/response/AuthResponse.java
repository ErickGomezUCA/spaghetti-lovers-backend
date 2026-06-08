package com.example.propertyrentalmanagement.dto.response;


import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        UserResponse user
) {


}
