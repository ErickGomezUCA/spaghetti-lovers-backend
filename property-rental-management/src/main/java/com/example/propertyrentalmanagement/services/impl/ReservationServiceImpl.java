package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.response.ReservationCancellationResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Notification;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.*;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.ReservationNotFoundException;
import com.example.propertyrentalmanagement.repositories.AvailabilityCalendarRepository;
import com.example.propertyrentalmanagement.repositories.NotificationRepository;
import com.example.propertyrentalmanagement.repositories.PaymentRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import com.example.propertyrentalmanagement.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final AvailabilityCalendarRepository availabilityCalendarRepository;
    private final NotificationRepository notificationRepository;
    private final AccessCodeService accessCodeService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public ReservationCancellationResponse cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        validateCancellationPermission(currentUser, reservation);
        validateReservationCanBeCancelled(reservation);

        LocalDate today = LocalDate.now();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, reservation.getCheckInDate());

        BigDecimal cancellationPenalty = calculateCancellationPenalty(reservation, daysUntilCheckIn);
        BigDecimal reservationRefundAmount = calculateReservationRefundAmount(reservation, cancellationPenalty);
        BigDecimal cleaningFeeRefundAmount = reservation.getCleaningFee();
        BigDecimal guaranteeDepositRefundAmount = refundGuaranteeDeposit(reservation);

        refundReservationPayment(reservation, reservationRefundAmount.add(cleaningFeeRefundAmount));

        LocalDateTime cancelledAt = LocalDateTime.now();

        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationPenalty(cancellationPenalty);
        reservation.setCancelledAt(cancelledAt);
        reservation.setUpdatedAt(cancelledAt);

        reservation.getProperty().setPropertyStatus(PropertyStatus.ACTIVE);

        availabilityCalendarRepository.deleteAll(
                availabilityCalendarRepository.findByReservation(reservation)
        );

        accessCodeService.invalidateCodesByReservation(reservation);

        createCancellationNotification(currentUser, reservation);

        reservationRepository.save(reservation);

        BigDecimal totalRefundAmount = reservationRefundAmount
                .add(cleaningFeeRefundAmount)
                .add(guaranteeDepositRefundAmount);

        return ReservationCancellationResponse.builder()
                .reservationId(reservation.getId())
                .reservationStatus(reservation.getReservationStatus())
                .cancellationPenalty(cancellationPenalty)
                .reservationRefundAmount(reservationRefundAmount)
                .cleaningFeeRefundAmount(cleaningFeeRefundAmount)
                .guaranteeDepositRefundAmount(guaranteeDepositRefundAmount)
                .totalRefundAmount(totalRefundAmount)
                .cancelledAt(cancelledAt)
                .build();
    }

    private void validateCancellationPermission(AppUser currentUser, Reservation reservation) {
        boolean isTenantOwner = reservation.getTenant().getId().equals(currentUser.getId());
        boolean isPropertyLandlord = reservation.getProperty().getLandlord().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isTenantOwner && !isPropertyLandlord && !isAdmin) {
            throw new NotResourceOwnerException("You are not allowed to cancel this reservation");
        }
    }

    private void validateReservationCanBeCancelled(Reservation reservation) {
        boolean isReservedOrActive = reservation.getReservationStatus() == ReservationStatus.RESERVED
                || reservation.getReservationStatus() == ReservationStatus.ACTIVE;

        if (!isReservedOrActive) {
            throw new IllegalStateException("Only reserved or active reservations can be cancelled");
        }

        if (!reservation.getCheckInDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel a reservation whose check-in date has already passed");
        }
    }

    private BigDecimal calculateCancellationPenalty(Reservation reservation, long daysUntilCheckIn) {
        if (daysUntilCheckIn >= 7) {
            return BigDecimal.ZERO;
        }

        if (daysUntilCheckIn >= 3) {
            return reservation.getBaseTotal().multiply(BigDecimal.valueOf(0.50));
        }

        return reservation.getBaseTotal();
    }

    private BigDecimal calculateReservationRefundAmount(Reservation reservation, BigDecimal cancellationPenalty) {
        return reservation.getBaseTotal().subtract(cancellationPenalty);
    }

    private void refundReservationPayment(Reservation reservation, BigDecimal refundAmount) {
        paymentRepository.findByReservationAndPaymentType(reservation, PaymentType.RESERVATION)
                .ifPresent(payment -> {
                    payment.setRefundAmount(refundAmount);
                    payment.setRefundedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                });
    }

    private BigDecimal refundGuaranteeDeposit(Reservation reservation) {
        return paymentRepository.findByReservationAndPaymentType(reservation, PaymentType.GUARANTEE_DEPOSIT)
                .map(payment -> {
                    payment.setRefundAmount(payment.getAmount());
                    payment.setRefundedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    return payment.getAmount();
                })
                .orElse(BigDecimal.ZERO);
    }

    private void createCancellationNotification(AppUser currentUser, Reservation reservation) {
        AppUser receiver = reservation.getTenant().getId().equals(currentUser.getId())
                ? reservation.getProperty().getLandlord()
                : reservation.getTenant();

        Notification notification = Notification.builder()
                .user(receiver)
                .reservation(reservation)
                .type(NotificationType.INFO)
                .title("Reserva cancelada")
                .message("La reserva ha sido cancelada.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }
}
