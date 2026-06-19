package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Fine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FineRepository extends JpaRepository<Fine, UUID> {
}
