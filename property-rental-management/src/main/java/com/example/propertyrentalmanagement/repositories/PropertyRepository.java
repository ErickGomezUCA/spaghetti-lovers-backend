package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Page<Property> findAllByLandlordId(UUID landlordId, Pageable pageable);
}
