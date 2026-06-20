package com.example.propertyrentalmanagement.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.propertyrentalmanagement.dto.response.FileUploadResponse;
import com.example.propertyrentalmanagement.exceptions.FileUploadException;
import com.example.propertyrentalmanagement.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(jpeg|png|webp)")) {
            throw new IllegalArgumentException("File must be an image (JPG, PNG or WEBP)");
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
                !filename.toLowerCase().matches(".*\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG and WEBP files are allowed");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "property-rental/images"
                    )
            );

            return new FileUploadResponse(
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id"),
                    "image"
            );

        } catch (IOException e) {
            throw new FileUploadException("Error uploading image to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadPdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("File must be a PDF");
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
                !filename.toLowerCase().matches(".*\\.pdf$")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        try {
            String publicId = "doc-" + java.util.UUID.randomUUID() + ".pdf";

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "property-rental/documents",
                            "public_id", publicId,
                            "use_filename", false,
                            "unique_filename", false
                    )
            );

            return new FileUploadResponse(
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id"),
                    "raw"
            );

        } catch (IOException e) {
            throw new FileUploadException("Error uploading PDF to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadGeneratedPdf(byte[] pdfBytes, String publicId) {

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("PDF content is required");
        }

        if (pdfBytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }

        try {
            String publicIdWithExtension = publicId.toLowerCase().endsWith(".pdf")
                    ? publicId
                    : publicId + ".pdf";

            Map uploadResult = cloudinary.uploader().upload(
                    pdfBytes,
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "property-rental/contracts",
                            "public_id", publicIdWithExtension,
                            "use_filename", false,
                            "unique_filename", false
                    )
            );

            return new FileUploadResponse(
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id"),
                    "raw"
            );

        } catch (IOException e) {
            throw new FileUploadException("Error uploading contract to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
        } catch (IOException e) {
            throw new FileUploadException("Error deleting file from Cloudinary: " + e.getMessage());
        }
    }
}