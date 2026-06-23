package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRole(com.example.propertyrentalmanagement.enums.UserRole role);
}

