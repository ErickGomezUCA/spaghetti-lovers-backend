package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    List<Property> findByLandlordId(UUID landlordId);
}
