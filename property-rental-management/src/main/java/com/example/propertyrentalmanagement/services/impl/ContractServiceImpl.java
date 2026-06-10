package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;
import com.example.propertyrentalmanagement.entitites.Contract;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ContractStatus;
import com.example.propertyrentalmanagement.repositories.ContractRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.services.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public ContractResponse createContract(CreateContractRequest contractRequest) {
        UUID reservationId = contractRequest.reservationId();

//        TODO: Get real reservation here, on task: [SPL-18] Reservas con fechas fijas (check-in/out)
        Reservation reservationFound = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // TODO: Include service to create PDF with reservation and property details here
        // TODO: Get URL from generated PDF

        String createdPdfURL = "http://localhost:8080/media/contract-file.pdf";

        Contract contract = Contract.builder()
                .reservation(reservationFound)
                .contentUrl(createdPdfURL)
                .contractStatus(ContractStatus.PENDING_SIGNATURES)
                .createdAtTimestamp(LocalDateTime.now())
                .expiresAtTimestamp(LocalDateTime.now().plusHours(48))
                .build();

        Contract createdContract = contractRepository.save(contract);
        return ContractResponse.fromEntity(createdContract);
    }

    @Override
    public ContractResponse signContract() {
        return null;
    }
}
