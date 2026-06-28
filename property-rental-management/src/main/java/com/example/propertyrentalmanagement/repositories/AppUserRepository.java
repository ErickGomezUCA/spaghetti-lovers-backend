package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.UserRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRole(UserRole role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM AppUser u WHERE u.id = :id")
    Optional<AppUser> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT u FROM AppUser u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AppUser> findWithFilters(
            @Param("role") UserRole role,
            @Param("search") String search,
            Pageable pageable
    );
}

