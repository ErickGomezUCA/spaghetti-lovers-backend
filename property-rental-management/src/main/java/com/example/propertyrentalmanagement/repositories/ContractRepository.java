package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Contract findContractByReservationId(UUID reservationId);
}
