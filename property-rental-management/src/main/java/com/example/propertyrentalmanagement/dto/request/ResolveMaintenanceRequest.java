package com.example.propertyrentalmanagement.dto.request;

import java.util.List;

public record ResolveMaintenanceRequest(
        String resolutionNotes,
        List<PhotoEntry> photoUrls
) {
    public record PhotoEntry(
            String url,
            String publicId
    ) {}
}
