package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateFineRequest;
import com.example.propertyrentalmanagement.dto.request.PayFineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.FineType;
import com.example.propertyrentalmanagement.enums.NotificationType;
import com.example.propertyrentalmanagement.enums.PaymentMethod;
import com.example.propertyrentalmanagement.enums.PaymentType;
import com.example.propertyrentalmanagement.exceptions.BadRequestException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.ReservationNotFoundException;
import com.example.propertyrentalmanagement.repositories.*;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    private final AuthenticatedUserProvider authProvider;

    @Override
    @Transactional
    public FineResponse createFine(CreateFineRequest request) {

        AppUser issuedBy = authProvider.getCurrentUser();

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        boolean isAdmin = issuedBy.getRole().name().equals("ADMIN") || issuedBy.getRole().name().equals("ROLE_ADMIN");

        if (!isAdmin) {
            if (!reservation.getProperty().getLandlord().getId().equals(issuedBy.getId())) {
                throw new NotResourceOwnerException("Permission denied: cannot issue fines for this property.");
            }
        }

        String status = reservation.getReservationStatus().name();
        FineType type = request.fineType();

        if (type == FineType.NOISE_VIOLATION || type == FineType.LATE_CHECKOUT) {
            if (!status.equals("ACTIVE")) {
                throw new BadRequestException("'ACTIVE' reservation required for this fine type.");
            }
        } else if (type == FineType.PROPERTY_DAMAGE) {
            if (!status.equals("ACTIVE") && !status.equals("COMPLETED")) {
                throw new BadRequestException("Property damage fines require an 'ACTIVE' or 'COMPLETED' reservation.");
            }
        }

        Payment payment = Payment.builder()
                .paymentType(PaymentType.FINE)
                .amount(request.amount())
                .paymentMethod(PaymentMethod.PENDING)
                .reservation(reservation)
                .createdAt(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);

        Fine fine = Fine.builder()
                .reservation(reservation)
                .issuedBy(issuedBy)
                .fineType(type)
                .description(request.description())
                .amount(request.amount())
                .payment(payment)
                .issuedAt(LocalDateTime.now())
                .build();
        fine = fineRepository.save(fine);

        Notification notification = Notification.builder()
                .user(reservation.getTenant())
                .type(NotificationType.INFO)
                .title("New Fine Issued")
                .message("Fine issued: " + request.amount() + " for " + type.name())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .reservation(reservation)
                .build();
        notificationRepository.save(notification);

        return FineResponse.fromEntity(fine, payment);
    }

    @Override
    @Transactional
    public FineResponse payFine(UUID fineId, PayFineRequest request) {

        AppUser currentTenant = authProvider.getCurrentUser();

        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new BadRequestException("Fine not found."));

        Reservation reservation = fine.getReservation();

        if (!reservation.getTenant().getId().equals(currentTenant.getId())) {
            throw new NotResourceOwnerException("Permission denied: You are not the tenant for this fine.");
        }

        if (fine.getResolvedAt() != null) {
            throw new BadRequestException("This fine has already been paid.");
        }

        Payment payment = fine.getPayment();
        payment.setPaymentMethod(request.paymentMethod());
        payment.setRefundAmount(java.math.BigDecimal.ZERO);
        payment = paymentRepository.save(payment);

        fine.setResolvedAt(LocalDateTime.now());
        fine = fineRepository.save(fine);

        AppUser landlord = reservation.getProperty().getLandlord();
        Notification notification = Notification.builder()
                .user(landlord)
                .type(NotificationType.INFO)
                .title("Fine Paid")
                .message("The tenant " + currentTenant.getName() + " has paid the fine of $" + fine.getAmount() + " for " + fine.getFineType().name() + ".")
                .createdAt(LocalDateTime.now())
                .reservation(reservation)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        return FineResponse.fromEntity(fine, payment);
    }
}