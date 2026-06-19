package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.response.ReservationCancellationResponse;
import com.example.propertyrentalmanagement.dto.response.ReservationCompletionResponse;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.*;
import com.example.propertyrentalmanagement.exceptions.*;
import com.example.propertyrentalmanagement.repositories.*;
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
import java.util.List;
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
    private final FineRepository fineRepository;

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
            throw new InvalidReservationCancellationException(
                    "Only reserved or active reservations can be cancelled"
            );
        }

        if (!reservation.getCheckInDate().isAfter(LocalDate.now())) {
            throw new InvalidReservationCancellationException(
                    "Cannot cancel a reservation whose check-in date has already passed"
            );
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

    @Override
    @Transactional
    public ReservationCompletionResponse completeReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        validateCompletionPermission(currentUser, reservation);
        validateReservationCanBeCompleted(reservation);

        Payment guaranteeDepositPayment = paymentRepository
                .findByReservationAndPaymentType(reservation, PaymentType.GUARANTEE_DEPOSIT)
                .orElseThrow(() -> new PaymentNotFoundException("Guarantee deposit payment not found"));

        List<Fine> openFines = fineRepository.findByReservationAndResolvedAtIsNull(reservation);

        List<Fine> openDamageFines = openFines.stream()
                .filter(fine -> fine.getFineType() == FineType.PROPERTY_DAMAGE)
                .toList();

        boolean hasOpenNonDamageFines = openFines.stream()
                .anyMatch(fine -> fine.getFineType() != FineType.PROPERTY_DAMAGE);

        if (hasOpenNonDamageFines) {
            throw new InvalidReservationCompletionException(
                    "Reservation has unresolved non-damage fines"
            );
        }

        BigDecimal guaranteeDepositAmount = guaranteeDepositPayment.getAmount();
        BigDecimal totalDamageFineAmount = calculateTotalFineAmount(openDamageFines);

        BigDecimal retainedAmount = guaranteeDepositAmount.min(totalDamageFineAmount);
        BigDecimal refundAmount = guaranteeDepositAmount.subtract(retainedAmount);
        BigDecimal additionalFinePaymentAmount = BigDecimal.ZERO;

        guaranteeDepositPayment.setRefundAmount(refundAmount);
        guaranteeDepositPayment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(guaranteeDepositPayment);

        if (totalDamageFineAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (totalDamageFineAmount.compareTo(guaranteeDepositAmount) <= 0) {
                markFinesAsResolved(openDamageFines);
            } else {
                additionalFinePaymentAmount = totalDamageFineAmount.subtract(guaranteeDepositAmount);
                createAdditionalFinePayment(
                        reservation,
                        guaranteeDepositPayment,
                        additionalFinePaymentAmount
                );
            }
        }

        LocalDateTime completedAt = LocalDateTime.now();

        reservation.setReservationStatus(ReservationStatus.COMPLETED);
        reservation.setUpdatedAt(completedAt);

        reservation.getProperty().setPropertyStatus(PropertyStatus.ACTIVE);

        availabilityCalendarRepository.deleteAll(
                availabilityCalendarRepository.findByReservation(reservation)
        );

        accessCodeService.invalidateCodesByReservation(reservation);

        createCompletionNotification(reservation, refundAmount, retainedAmount);

        reservationRepository.save(reservation);

        return ReservationCompletionResponse.builder()
                .reservationId(reservation.getId())
                .reservationStatus(reservation.getReservationStatus())
                .guaranteeDepositAmount(guaranteeDepositAmount)
                .retainedAmount(retainedAmount)
                .guaranteeDepositRefundAmount(refundAmount)
                .additionalFinePaymentAmount(additionalFinePaymentAmount)
                .completedAt(completedAt)
                .build();
    }

    private void validateCompletionPermission(AppUser currentUser, Reservation reservation) {
        boolean isPropertyLandlord = reservation.getProperty().getLandlord().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isPropertyLandlord && !isAdmin) {
            throw new NotResourceOwnerException("Only the property landlord or admin can complete this reservation");
        }
    }

    private void validateReservationCanBeCompleted(Reservation reservation) {
        if (reservation.getReservationStatus() != ReservationStatus.ACTIVE) {
            throw new InvalidReservationCompletionException(
                    "Only active reservations can be completed"
            );
        }

        if (reservation.getCheckOutDate().isAfter(LocalDate.now())) {
            throw new InvalidReservationCompletionException(
                    "Cannot complete a reservation before check-out date"
            );
        }
    }
    private BigDecimal calculateTotalFineAmount(List<Fine> fines) {
        return fines.stream()
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private void markFinesAsResolved(List<Fine> fines) {
        LocalDateTime resolvedAt = LocalDateTime.now();

        fines.forEach(fine -> fine.setResolvedAt(resolvedAt));

        fineRepository.saveAll(fines);
    }
    private void createAdditionalFinePayment(
            Reservation reservation,
            Payment guaranteeDepositPayment,
            BigDecimal additionalFinePaymentAmount
    ) {
        Payment additionalFinePayment = Payment.builder()
                .reservation(reservation)
                .amount(additionalFinePaymentAmount)
                .paymentType(PaymentType.FINE)
                .paymentMethod(guaranteeDepositPayment.getPaymentMethod())
                .refundAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(additionalFinePayment);
    }
    private void createCompletionNotification(
            Reservation reservation,
            BigDecimal refundAmount,
            BigDecimal retainedAmount
    ) {
        String message = retainedAmount.compareTo(BigDecimal.ZERO) > 0
                ? "La reserva fue completada. Se reembolsó parte del depósito de garantía y se retuvo un monto por daños."
                : "La reserva fue completada. El depósito de garantía fue reembolsado completamente.";

        Notification notification = Notification.builder()
                .user(reservation.getTenant())
                .reservation(reservation)
                .type(NotificationType.INFO)
                .title("Reserva completada")
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }
}
