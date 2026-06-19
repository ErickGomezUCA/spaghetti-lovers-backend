package com.example.propertyrentalmanagement.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AttachPhotoRequest(
        @NotEmpty
        List<PhotoEntry> photoUrls
) {
        public record PhotoEntry(
                String url,
                String publicId
        ) {}
}
