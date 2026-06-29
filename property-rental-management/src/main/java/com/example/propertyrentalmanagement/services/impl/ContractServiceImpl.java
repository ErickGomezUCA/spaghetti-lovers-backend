package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.response.ContractResponse;
import com.example.propertyrentalmanagement.dto.response.FileUploadResponse;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.ContractStatus;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.ContractAlreadyExistsException;
import com.example.propertyrentalmanagement.exceptions.ContractNotFoundException;
import com.example.propertyrentalmanagement.exceptions.InvalidContractException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.ContractRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.CloudinaryService;
import com.example.propertyrentalmanagement.services.ContractService;
import com.example.propertyrentalmanagement.services.SignatureService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.example.propertyrentalmanagement.enums.NotificationType;
import com.example.propertyrentalmanagement.repositories.NotificationRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final CloudinaryService cloudinaryService;
    private final ReservationRepository reservationRepository; // TODO: See later if it's necessary to call services instead of repos
    private final PropertyRepository propertyRepository;
    private final AppUserRepository appUserRepository;
    private final SignatureService signatureService;
    private final TemplateEngine templateEngine;
    private final AuthenticatedUserProvider authProvider;
    private final NotificationRepository notificationRepository;

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

        byte[] pdfBytes = generateContractPdf(reservationFound);
        FileUploadResponse uploadResult = cloudinaryService.uploadGeneratedPdf(pdfBytes, "contract-" + reservationId);
        String createdPdfURL = uploadResult.url();

        Contract contract = Contract.builder()
                .reservation(reservationFound)
                .contentUrl(createdPdfURL)
                .contractStatus(ContractStatus.PENDING_SIGNATURES)
                .createdAtTimestamp(LocalDateTime.now())
                .expiresAtTimestamp(LocalDateTime.now().plusHours(48))
                .build();

        Contract createdContract = contractRepository.save(contract);

        String formattedExpirationDate = contract.getExpiresAtTimestamp()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        Notification notification = Notification.builder()
                .user(reservationFound.getTenant())
                .reservation(reservationFound)
                .type(NotificationType.INFO)
                .title("Contrato pendiente de firma")
                .message("Tienes un contrato pendiente de firma para tu reserva en "
                        + reservationFound.getProperty().getTitle()
                        + ". Firma antes del "
                        + formattedExpirationDate
                        + ".")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        return ContractResponse.fromEntity(createdContract);
    }

    @Override
    public ContractResponse signContract(UUID contractId) {
        AppUser authUser = authProvider.getCurrentUser();

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("Contract not found"));

        if (contract.getContractStatus() == ContractStatus.CANCELLED) {
            throw new InvalidContractException("Cancelled contracts cannot be signed");
        }

        Reservation reservation = contract.getReservation();
        Property property = reservation.getProperty();

        if ((authUser.getRole() == UserRole.TENANT && !reservation.getTenant().getId().equals(authUser.getId()))
                || (authUser.getRole() == UserRole.LANDLORD && !property.getLandlord().getId().equals(authUser.getId()))) {
            throw new InvalidContractException("User is not allowed to sign this contract");
        }

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

        if (authUser.getRole() == UserRole.TENANT) {
            if (contract.getTenantSignature() != null) {
                throw new InvalidContractException("Tenant has already signed the contract");
            }

            Signature tenantSignature = signatureService.createSignature(authUser.getId(), contractId);
            contractToSign.setTenantSignature(tenantSignature);

        } else if (authUser.getRole() == UserRole.LANDLORD) {
            if (contract.getLandlordSignature() != null) {
                throw new InvalidContractException("Landlord has already signed the contract");
            }

            Signature landlordSignature = signatureService.createSignature(authUser.getId(), contractId);
            contractToSign.setLandlordSignature(landlordSignature);
        } else {
            throw new InvalidContractException("User is not allowed to sign this contract");
        }

        if (contractToSign.getTenantSignature() != null && contractToSign.getLandlordSignature() != null) {
            contractToSign.setContractStatus(ContractStatus.SIGNED);
        }

        Contract signedContract = contractRepository.save(contractToSign);

        if (authUser.getRole() == UserRole.TENANT) {
            Notification notification = Notification.builder()
                    .user(property.getLandlord())
                    .reservation(reservation)
                    .type(NotificationType.INFO)
                    .title("Contrato firmado")
                    .message("El contrato de la reserva en "
                            + property.getTitle()
                            + " ha sido firmado por el inquilino "
                            + authUser.getName()
                            + ".")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }

        return ContractResponse.fromEntity(signedContract);
    }

    @Override
    public List<ContractResponse> getContractsByUser(AppUser user) {
        List<Contract> contracts = switch (user.getRole()) {
            case TENANT -> contractRepository.findByReservationTenantId(user.getId());
            case LANDLORD -> contractRepository.findByReservationPropertyLandlordId(user.getId());
            default -> List.of();
        };
        return contracts.stream().map(ContractResponse::fromEntity).toList();
    }

    @Override
    public ContractResponse getContractByReservationId(UUID reservationId) {
        Contract contract = contractRepository.findContractByReservationId(reservationId);

        if (contract == null) {
            return null;
        }

        return ContractResponse.fromEntity(contract);
    }

    public void processContractExtension(Reservation reservation) {
        Contract contract = contractRepository.findContractByReservationId(reservation.getId());

        if (contract == null) {
            return;
        }

        if (contract.getContractStatus() == ContractStatus.SIGNED) {
            contract.setContractStatus(ContractStatus.PENDING_SIGNATURES);
            contract.setTenantSignature(null);
            contract.setLandlordSignature(null);
        }

        contract.setExpiresAtTimestamp(LocalDateTime.now().plusHours(48));

        byte[] pdfBytes = generateContractPdf(reservation);
        String newUrl = cloudinaryService.uploadGeneratedPdf(pdfBytes, "contract-ext-" + reservation.getId()).url();
        contract.setContentUrl(newUrl);

        contractRepository.save(contract);
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

    @Override
    public void cancelContractByReservation(Reservation reservation) {
        Contract contract = contractRepository.findContractByReservationId(
                reservation.getId()
        );

        if (contract == null) {
            return;
        }

        contract.setContractStatus(ContractStatus.CANCELLED);
        contractRepository.save(contract);
    }
}
