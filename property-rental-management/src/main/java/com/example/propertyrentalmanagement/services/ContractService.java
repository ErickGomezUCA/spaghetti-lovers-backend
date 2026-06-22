package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Reservation;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    ContractResponse createContract(CreateContractRequest contractRequest);

    ContractResponse signContract(UUID contractId);

    ContractResponse getContractByReservationId(UUID reservationId);

    List<ContractResponse> getContractsByUser(AppUser user);

    void processContractExtension(Reservation reservation);
}
