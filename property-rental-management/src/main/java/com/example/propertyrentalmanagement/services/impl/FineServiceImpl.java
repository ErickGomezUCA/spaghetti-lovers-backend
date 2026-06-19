package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.FineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.FineType;
import com.example.propertyrentalmanagement.enums.NotificationType;
import com.example.propertyrentalmanagement.enums.PaymentMethod;
import com.example.propertyrentalmanagement.enums.PaymentType;
import com.example.propertyrentalmanagement.exceptions.BadRequestException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.ReservationNotFoundException;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.*;
import com.example.propertyrentalmanagement.services.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final AppUserRepository userRepository;

    @Override
    @Transactional
    public FineResponse createFine(FineRequest request, String currentUsername, boolean isAdmin) {

        AppUser issuedBy = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(""));

        if (!isAdmin) {
            if (!reservation.getProperty().getLandlord().getId().equals(issuedBy.getId())) {
                throw new NotResourceOwnerException("Permission denied: cannot issue fines for this property.");            }
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

        return FineResponse.fromEntity(fine, payment);    }
}