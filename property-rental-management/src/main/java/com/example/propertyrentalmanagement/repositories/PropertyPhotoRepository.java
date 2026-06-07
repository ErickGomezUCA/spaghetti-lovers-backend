package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.PropertyPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, UUID> {
}

