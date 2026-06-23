package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.IdentityDocument;
import com.example.propertyrentalmanagement.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, UUID> {

    boolean existsByUserAndDocumentStatus(AppUser user, DocumentStatus status);
}