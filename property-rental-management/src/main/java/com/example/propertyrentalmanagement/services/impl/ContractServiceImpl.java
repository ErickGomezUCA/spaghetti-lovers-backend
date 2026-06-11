package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.ContractStatus;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.ContractAlreadyExistsException;
import com.example.propertyrentalmanagement.exceptions.ContractNotFoundException;
import com.example.propertyrentalmanagement.exceptions.InvalidContractException;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.ContractRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.services.ContractService;
import com.example.propertyrentalmanagement.services.SignatureService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final FileStorageService fileStorageService;
    private final ReservationRepository reservationRepository; // TODO: See later if it's necessary to call services instead of repos
    private final PropertyRepository propertyRepository;
    private final AppUserRepository appUserRepository;
    private final SignatureService signatureService;
    private final TemplateEngine templateEngine;

    @Override
    public ContractResponse createContract(CreateContractRequest contractRequest) {
        UUID reservationId = contractRequest.reservationId();
        Contract conflictingContract = contractRepository.findContractByReservationId(reservationId);
        if (conflictingContract != null) {
            throw new ContractAlreadyExistsException("Contract already exists for this reservation");
        }

//        TODO: Get real reservation here, on task: [SPL-18] Reservas con fechas fijas (check-in/out)
        Reservation reservationFound = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // TODO: Replace local storage service into cloud service, on task: [SPL-32] Almacenamiento en nube para archivos multimedia
        byte[] pdfBytes = generateContractPdf(reservationFound);
        String createdPdfURL = fileStorageService.storePdf(pdfBytes, reservationId);

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
    public ContractResponse signContract(UUID contractId, UUID userId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("Contract not found"));
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Reservation reservation = contract.getReservation();
        Property property = reservation.getProperty();

        if (contract.getContractStatus() == ContractStatus.SIGNED) {
            throw new InvalidContractException("Contract already signed");
        }

        if (contract.getExpiresAtTimestamp().isBefore(LocalDateTime.now())) {
            throw new InvalidContractException("Contract has expired");
        }

        Contract contractToSign = Contract.builder()
                .id(contract.getId())
                .reservation(reservation)
                .contentUrl(contract.getContentUrl())
                .contractStatus(contract.getContractStatus())
                .tenantSignature(contract.getTenantSignature())
                .landlordSignature(contract.getLandlordSignature())
                .createdAtTimestamp(contract.getCreatedAtTimestamp())
                .expiresAtTimestamp(contract.getExpiresAtTimestamp())
                .build();

        if (user.getRole() == UserRole.TENANT) {
            if (contract.getTenantSignature() != null) {
                throw new InvalidContractException("Tenant has already signed the contract");
            }

            // TODO: Pending to test reservation and property ownership
            if (reservation.getTenant().getId() != userId) {
                throw new InvalidContractException("User is not the tenant of this reservation");
            }

            Signature tenantSignature = signatureService.createSignature(userId, contractId);
            contractToSign.setTenantSignature(tenantSignature);

        } else if (user.getRole() == UserRole.LANDLORD) {
            if (contract.getLandlordSignature() != null) {
                throw new InvalidContractException("Landlord has already signed the contract");
            }

            if (property.getLandlord().getId() != userId) {
                throw new InvalidContractException("User is not the landlord of this property");
            }

            Signature landlordSignature = signatureService.createSignature(userId, contractId);
            contractToSign.setLandlordSignature(landlordSignature);
        } else {
            throw new InvalidContractException("User is not allowed to sign contract");
        }

        if (contractToSign.getTenantSignature() != null && contractToSign.getLandlordSignature() != null) {
            contractToSign.setContractStatus(ContractStatus.SIGNED);
        }

        Contract signedContract = contractRepository.save(contractToSign);
        return ContractResponse.fromEntity(signedContract);
    }

    private byte[] generateContractPdf(Reservation reservation) {
        Property property = reservation.getProperty();

        Context context = new Context();
        context.setVariable("propertyTitle", property.getTitle());
        context.setVariable("propertyAddress", property.getAddress());
        context.setVariable("landlordName", property.getLandlord().getName());
        context.setVariable("tenantName", reservation.getTenant().getName());
        context.setVariable("checkInDate", reservation.getCheckInDate());
        context.setVariable("checkOutDate", reservation.getCheckOutDate());
        context.setVariable("totalNights", reservation.getTotalNights());
        context.setVariable("baseTotal", reservation.getBaseTotal());
        context.setVariable("cleaningFee", reservation.getCleaningFee());
        context.setVariable("securityDeposit", property.getSecurityDepositAmount());
        context.setVariable("cancellationPenalty", reservation.getCancellationPenalty());
        context.setVariable("totalPrice", reservation.getTotalPrice());
        context.setVariable("rules", property.getRules());
        context.setVariable("generatedAt",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String htmlContent = templateEngine.process("contract", context);

        return renderPdf(htmlContent);
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating contract PDF", e);
        }
    }
}
