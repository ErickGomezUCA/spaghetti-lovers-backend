package com.example.propertyrentalmanagement.dto.request;

import java.util.List;

public record ResolveMaintenanceRequest(
        String resolutionNotes,
        List<String> photoUrls
) {
}
