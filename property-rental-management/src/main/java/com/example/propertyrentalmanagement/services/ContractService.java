package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;

public interface ContractService {
    ContractResponse createContract(CreateContractRequest contractRequest);

    ContractResponse signContract();
}
