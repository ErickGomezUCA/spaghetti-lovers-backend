package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
