package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    FileUploadResponse uploadImage(MultipartFile file);
    FileUploadResponse uploadPdf(MultipartFile file);
    FileUploadResponse uploadGeneratedPdf(byte[] pdfBytes, String publicId);
    void deleteFile(String publicId, String resourceType);
}