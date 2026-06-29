package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.PropertyPhoto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, UUID> {

    @Query("SELECT p FROM PropertyPhoto p WHERE p.property.id IN :propertyIds")
    List<PropertyPhoto> findByPropertyIdIn(@Param("propertyIds") List<UUID> propertyIds);
}

