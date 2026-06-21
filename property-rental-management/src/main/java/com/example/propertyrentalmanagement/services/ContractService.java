package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;
import com.example.propertyrentalmanagement.entitites.Reservation;

import java.util.UUID;

public interface ContractService {
    ContractResponse createContract(CreateContractRequest contractRequest);

    ContractResponse signContract(UUID contractId);

    ContractResponse getContractByReservationId(UUID reservationId);

    void processContractExtension(Reservation reservation);
}
