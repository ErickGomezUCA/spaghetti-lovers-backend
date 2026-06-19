package com.example.propertyrentalmanagement.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// TODO: Replace this service into proper cloud service, on task [SPL-32] Almacenamiento en nube para archivos multimedia
@Service
@RequiredArgsConstructor
public class FileStorageService {
    @Value("${media.path}")
    private String mediaPath;
    @Value("${media.base-url}")
    private String baseUrl;

    public String storePdf(byte[] content, UUID reservationId) {
        try {
            Path dir = Paths.get(mediaPath, "contracts");
            Files.createDirectories(dir);

            String filename = "contract-" + reservationId + ".pdf";
            Path filePath = dir.resolve(filename);
            Files.write(filePath, content);

            return baseUrl + "/contracts/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error storing contract PDF", e);
        }
    }
}
