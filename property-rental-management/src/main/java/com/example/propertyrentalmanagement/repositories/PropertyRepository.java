package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.enums.PropertyStatus;
import com.example.propertyrentalmanagement.enums.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Page<Property> findAllByLandlordId(UUID landlordId, Pageable pageable);

    Long countByLandlordId(UUID landlordId);

    @Query("SELECT p FROM Property p WHERE " +
           "(CAST(:term AS string) IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%')) " +
           "OR LOWER(p.city) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%')) " +
           "OR LOWER(p.address) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))) " +
           "AND (:propertyType IS NULL OR p.propertyType = :propertyType) " +
           "AND (:minGuests IS NULL OR p.maxGuests >= :minGuests) " +
           "AND (:status IS NULL OR p.propertyStatus = :status)")
    Page<Property> searchProperties(
            @Param("term") String term,
            @Param("propertyType") PropertyType propertyType,
            @Param("minGuests") Integer minGuests,
            @Param("status") PropertyStatus status,
            Pageable pageable
    );
}
