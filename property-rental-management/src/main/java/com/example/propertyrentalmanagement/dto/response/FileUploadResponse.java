package com.example.propertyrentalmanagement.dto.response;

public record FileUploadResponse (
        String url,
        String publicId,
        String resourceType
){
}
