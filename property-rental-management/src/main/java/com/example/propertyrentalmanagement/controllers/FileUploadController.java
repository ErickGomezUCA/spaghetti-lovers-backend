package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.response.FileUploadResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('LANDLORD', 'TENANT')")
    ResponseEntity<GenericResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = cloudinaryService.uploadImage(file);
        return GenericResponse.builder()
                .message("Image uploaded successfully")
                .data(response)
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @PostMapping("/pdf")
    @PreAuthorize("hasAnyRole('LANDLORD', 'TENANT')")
    ResponseEntity<GenericResponse> uploadPdf(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = cloudinaryService.uploadPdf(file);
        return GenericResponse.builder()
                .message("PDF uploaded successfully")
                .data(response)
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }
}