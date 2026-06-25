package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    // Endpoint is protected by admin, but this service action can be triggered on Create Reservation event
    @PreAuthorize("@authorizationService.isAdmin()")
    @PostMapping
    ResponseEntity<GenericResponse> createContract(@Valid @RequestBody CreateContractRequest createContractRequest) {
        ContractResponse createdContract = contractService.createContract(createContractRequest);

        return GenericResponse.builder()
                .message("Contract created successfully")
                .data(createdContract)
                .resourceId(createdContract.id())
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @GetMapping("/me")
    ResponseEntity<GenericResponse> getMyContracts() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();
        List<ContractResponse> contracts = contractService.getContractsByUser(currentUser);

        return GenericResponse.builder()
                .message("Contracts found")
                .data(contracts)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }

    @PostMapping("/{id}/sign")
    ResponseEntity<GenericResponse> signContract(@PathVariable UUID id) {
        ContractResponse signedContract = contractService.signContract(id);

        return GenericResponse.builder()
                .message("Contract signed successfully")
                .data(signedContract)
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
