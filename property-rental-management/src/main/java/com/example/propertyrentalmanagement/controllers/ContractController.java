package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    //    TODO: Protect routes by role
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

    @PostMapping("/{id}/sign")
    ResponseEntity<GenericResponse> signContract(@RequestParam(name = "userId") UUID userId, @PathVariable UUID id) {
        ContractResponse signedContract = contractService.signContract(id, userId);

        return GenericResponse.builder()
                .message("Contract signed successfully")
                .data(signedContract)
                .resourceId(signedContract.id())
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
