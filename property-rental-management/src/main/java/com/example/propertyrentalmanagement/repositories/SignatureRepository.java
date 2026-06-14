package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Signature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, UUID> {
}
