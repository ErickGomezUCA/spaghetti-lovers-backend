package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.ReviewIdentityDocumentRequest;
import com.example.propertyrentalmanagement.dto.request.SubmitIdentityDocumentRequest;
import com.example.propertyrentalmanagement.dto.response.IdentityDocumentResponse;
import com.example.propertyrentalmanagement.enums.DocumentStatus;

import java.util.List;
import java.util.UUID;

public interface IdentityDocumentService {
    IdentityDocumentResponse submitDocument(SubmitIdentityDocumentRequest request);

    IdentityDocumentResponse reviewDocument(UUID documentId, ReviewIdentityDocumentRequest request);

    List<IdentityDocumentResponse> getAllDocuments(DocumentStatus status);
}