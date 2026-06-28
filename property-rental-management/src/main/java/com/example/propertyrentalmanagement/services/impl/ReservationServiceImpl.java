package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateContractRequest;
import com.example.propertyrentalmanagement.dto.request.CreateReservationRequest;
import com.example.propertyrentalmanagement.dto.request.ExtendReservationRequest;
import com.example.propertyrentalmanagement.dto.response.*;
import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.*;
import com.example.propertyrentalmanagement.exceptions.*;
import com.example.propertyrentalmanagement.repositories.*;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import com.example.propertyrentalmanagement.services.ContractService;
import com.example.propertyrentalmanagement.services.ReservationService;
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final PropertyRepository propertyRepository;
    private final ContractService contractService;

    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        AppUser tenant = authenticatedUserProvider.getCurrentUser();

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        if (property.getPropertyStatus() == PropertyStatus.UNAVAILABLE) {
            throw new BadRequestException("Property is not available for new reservations.");
        }

        if (request.paymentMethod() == PaymentMethod.PENDING) {
            throw new BadRequestException("Payment method cannot be PENDING for new reservations.");
        }

        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new BadRequestException("Check-out date must be after check-in date.");
        }

        LocalDateTime checkInTime = request.checkInDate().atTime(13, 0);
        LocalDateTime checkOutTime = request.checkOutDate().atTime(11, 0);

        List<AvailabilityCalendar> overlaps = availabilityCalendarRepository
                .findOverlappingBlocks(property.getId(), checkInTime, checkOutTime);

        if (!overlaps.isEmpty()) {
            throw new ConflictException("The property is already booked for these dates.");
        }

        if (reservationRepository.hasOverlappingReservations(tenant.getId(), request.checkInDate(), request.checkOutDate())) {
            throw new ConflictException("You already have an active reservation during these dates.");
        }

        long totalNights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());

        BigDecimal baseTotal = property.getBasePricePerNight()
                .multiply(BigDecimal.valueOf(totalNights))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal cleaningFee = property.getCleaningFee().setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = totalNights >= 28
                ? baseTotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal securityDeposit = property.getSecurityDepositAmount().setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPrice = baseTotal.add(cleaningFee).subtract(discount).add(securityDeposit)
                .setScale(2, RoundingMode.HALF_UP);

        Reservation reservation = Reservation.builder()
                .property(property)
                .tenant(tenant)
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .guestsCount(request.guestsCount())
                .totalNights((int) totalNights)
                .baseTotal(baseTotal)
                .cleaningFee(cleaningFee)
                .longStayDiscount(discount)
                .totalPrice(totalPrice)
                .reservationStatus(ReservationStatus.RESERVED)
                .createdAt(LocalDateTime.now())
                .build();

        reservation = reservationRepository.save(reservation);

        createPayment(reservation, baseTotal.add(cleaningFee).subtract(discount), PaymentType.RESERVATION, request.paymentMethod());
        createPayment(reservation, securityDeposit, PaymentType.GUARANTEE_DEPOSIT, request.paymentMethod());

        Notification tenantReservationNotification = Notification.builder()
                .user(tenant)
                .reservation(reservation)
                .type(NotificationType.INFO)
                .title("Reserva confirmada")
                .message("Tu reserva en "
                        + property.getTitle()
                        + " ha sido confirmada para el "
                        + reservation.getCheckInDate()
                        + " al "
                        + reservation.getCheckOutDate()
                        + ".")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(tenantReservationNotification);

        AvailabilityCalendar block = AvailabilityCalendar.builder()
                .property(property)
                .timestampStart(checkInTime)
                .timestampEnd(checkOutTime)
                .blockType(BlockType.RESERVATION)
                .reservation(reservation)
                .blockedReason(tenant.getName())
                .build();
        availabilityCalendarRepository.save(block);

        property.setPropertyStatus(PropertyStatus.RESERVED);
        propertyRepository.save(property);

        Notification notification = Notification.builder()
                .user(property.getLandlord())
                .reservation(reservation)
                .type(NotificationType.INFO)
                .title("New Reservation")
                .message("You have a new reservation for your property.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        accessCodeService.generateAccessCodeForReservation(reservation);

        Notification accessCodeNotification = Notification.builder()
                .user(tenant)
                .reservation(reservation)
                .type(NotificationType.INFO)
                .title("Código de acceso generado")
                .message("Tu código de acceso para la propiedad "
                        + property.getTitle()
                        + " ha sido generado y estará disponible para tu reserva.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(accessCodeNotification);

        contractService.createContract(new CreateContractRequest(reservation.getId()));

        return ReservationResponse.fromEntity(reservation);
    }

    private void createPayment(Reservation res, BigDecimal amount, PaymentType type, PaymentMethod method) {
        Payment p = Payment.builder()
                .reservation(res)
                .amount(amount)
                .paymentType(type)
                .paymentMethod(method)
                .refundAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDetailResponse getReservationById(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        boolean isTenantOwner = reservation.getTenant().getId().equals(currentUser.getId());
        boolean isPropertyLandlord = reservation.getProperty().getLandlord().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isTenantOwner && !isPropertyLandlord && !isAdmin) {
            throw new NotResourceOwnerException("You do not have permission to view this reservation.");
        }

        List<Payment> payments = paymentRepository.findByReservation(reservation);
        List<PaymentResponse> paymentResponses = payments.stream()
                .map(PaymentResponse::fromEntity)
                .toList();

        ContractResponse contractResponse = contractService.getContractByReservationId(reservationId);

        PropertySummaryResponse propertySummary = PropertySummaryResponse.fromEntity(reservation.getProperty());

        String activeAccessCode = null;
        try {
            activeAccessCode = accessCodeService.getActiveAccessCodeByReservationId(reservationId).code();
        } catch (AccessCodeNotFoundException | IllegalStateException ex) {
            // No active/valid access code available yet; keep null
        }

        return ReservationDetailResponse.fromEntity(
                reservation,
                propertySummary,
                activeAccessCode,
                paymentResponses,
                contractResponse
        );
    }

    @Override
    @Transactional
    public ReservationCancellationResponse cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        validateCancellationPermission(currentUser, reservation);
        validateReservationCanBeCancelled(reservation);

        ReservationCancellationPreviewResponse preview = buildCancellationPreview(reservation, currentUser);

        BigDecimal cancellationPenalty = preview.cancellationPenalty();
        BigDecimal reservationRefundAmount = preview.reservationRefundAmount();
        BigDecimal cleaningFeeRefundAmount = preview.cleaningFeeRefundAmount();
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

        return ReservationCancellationResponse.fromEntity(
                reservation,
                reservationRefundAmount,
                cleaningFeeRefundAmount,
                guaranteeDepositRefundAmount,
                totalRefundAmount
        );
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

        LocalDate today = LocalDate.now();

        if (reservation.getCheckInDate().isBefore(today)) {
            throw new InvalidReservationCancellationException(
                    "Cannot cancel a reservation whose check-in date has already passed"
            );
        }
    }

    private BigDecimal calculateCancellationPenalty(
            Reservation reservation,
            long daysUntilCheckIn,
            AppUser currentUser
    ) {
        boolean isTenantOwner = reservation.getTenant().getId().equals(currentUser.getId());

        if (!isTenantOwner) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (daysUntilCheckIn >= 7) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (daysUntilCheckIn >= 3) {
            return reservation.getBaseTotal()
                    .multiply(new BigDecimal("0.50"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return reservation.getBaseTotal().setScale(2, RoundingMode.HALF_UP);
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
                .title("Reservation Cancelled")
                .message("The reservation has been cancelled.")
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

                markFinesAsResolved(openDamageFines);
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

        return ReservationCompletionResponse.fromEntity(
                reservation,
                guaranteeDepositAmount,
                retainedAmount,
                refundAmount,
                additionalFinePaymentAmount,
                completedAt
        );
    }

    @Override
    @Transactional
    public ReservationExtensionResponse extendReservation(UUID reservationId, ExtendReservationRequest request) {
        AppUser currentTenant = authenticatedUserProvider.getCurrentUser();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (!reservation.getTenant().getId().equals(currentTenant.getId())) {
            throw new NotResourceOwnerException("You are not allowed to extend this reservation.");
        }

        String status = reservation.getReservationStatus().name();
        if (!status.equals("RESERVED") && !status.equals("ACTIVE")) {
            throw new BadRequestException("Only RESERVED or ACTIVE reservations can be extended.");
        }

        if (!request.newCheckOutDate().isAfter(reservation.getCheckOutDate())) {
            throw new BadRequestException("The new check-out date must be after the current check-out date.");
        }

        if (request.paymentMethod() == PaymentMethod.PENDING) {
            throw new BadRequestException("A valid payment method is required. 'PENDING' is not allowed for extensions.");
        }

        LocalDateTime currentCheckOutTime = reservation.getCheckOutDate().atTime(11, 0);
        LocalDateTime newCheckOutTime = request.newCheckOutDate().atTime(11, 0);

        List<AvailabilityCalendar> overlaps = availabilityCalendarRepository.findExtensionOverlaps(
                reservation.getProperty().getId(),
                newCheckOutTime,
                currentCheckOutTime,
                reservation.getId()
        );

        if (!overlaps.isEmpty()) {
            throw new ConflictException("The property is not available for the requested extension dates.");
        }

        long additionalNights = ChronoUnit.DAYS.between(reservation.getCheckOutDate(), request.newCheckOutDate());

        BigDecimal extensionAmount = reservation.getProperty().getBasePricePerNight()
                .multiply(BigDecimal.valueOf(additionalNights))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        Payment extensionPayment = Payment.builder()
                .reservation(reservation)
                .amount(extensionAmount)
                .paymentType(PaymentType.EXTENSION)
                .paymentMethod(request.paymentMethod())
                .refundAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        extensionPayment = paymentRepository.save(extensionPayment);

        reservation.setCheckOutDate(request.newCheckOutDate());
        reservation.setTotalNights(reservation.getTotalNights() + (int) additionalNights);
        reservation.setBaseTotal(reservation.getBaseTotal().add(extensionAmount));
        reservation.setTotalPrice(reservation.getTotalPrice().add(extensionAmount));
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        List<AvailabilityCalendar> calendarBlocks = availabilityCalendarRepository.findByReservation(reservation);

        if (!calendarBlocks.isEmpty()) {
            AvailabilityCalendar calendar = calendarBlocks.getFirst();
            calendar.setTimestampEnd(newCheckOutTime);
            availabilityCalendarRepository.save(calendar);
        }

        accessCodeService.extendAccessCodesValidUntil(reservation, newCheckOutTime);

        contractService.processContractExtension(reservation);

        Notification notification = Notification.builder()
                .user(reservation.getProperty().getLandlord())
                .reservation(reservation)
                .type(NotificationType.INFO)
                .title("Reservation Extended")
                .message("The tenant has extended their reservation. New check-out date is " + request.newCheckOutDate() + ".")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        return ReservationExtensionResponse.fromEntity(reservation, extensionPayment);
    }

    @Override
    public Page<ReservationResponse> getMyReservations(int page, int pageSize, String sortBy, String sortOrder, ReservationStatus status) {
        int safePage = Math.max(page, 0);
        int safePageSize = Math.clamp(pageSize, 1, 100);

        Pageable pageable = PaginationUtils.getPageRequest(safePage, safePageSize, sortBy, sortOrder);
        UUID currentUserId = authenticatedUserProvider.getCurrentUser().getId();

        Page<Reservation> reservationsPage;

        if (status == null) {
            reservationsPage = reservationRepository.findByTenantId(currentUserId, pageable);
        } else {
            reservationsPage = reservationRepository.findByTenantIdAndReservationStatus(currentUserId, status, pageable);
        }

        return reservationsPage.map(ReservationResponse::fromEntity);
    }

    @Override
    public Page<ReservationResponse> getLandlordReservations(int page, int pageSize, String sortBy, String sortOrder, ReservationStatus status, String searchTerm) {
        int safePage = Math.max(page, 0);
        int safePageSize = Math.clamp(pageSize, 1, 100);

        Pageable pageable = PaginationUtils.getPageRequest(safePage, safePageSize, sortBy, sortOrder);
        UUID currentLandlordId = authenticatedUserProvider.getCurrentUser().getId();
        String normalizedSearchTerm = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.trim();

        Page<Reservation> reservationsPage = reservationRepository.findLandlordReservationsWithFilters(
                currentLandlordId,
                status,
                normalizedSearchTerm,
                pageable
        );

        return reservationsPage.map(ReservationResponse::fromEntity);
    }

    @Override
    public LandlordReservationSummaryResponse getLandlordReservationSummary() {
        UUID currentLandlordId = authenticatedUserProvider.getCurrentUser().getId();

        List<ReservationRepository.StatusCount> statusCounts = reservationRepository.countReservationsGroupedByStatus(currentLandlordId);

        long reserved = 0, active = 0, completed = 0, cancelled = 0;

        for (ReservationRepository.StatusCount sc : statusCounts) {
            switch (sc.getStatus()) {
                case RESERVED -> reserved = sc.getCount();
                case ACTIVE -> active = sc.getCount();
                case COMPLETED -> completed = sc.getCount();
                case CANCELLED -> cancelled = sc.getCount();
            }
        }

        return new LandlordReservationSummaryResponse(reserved, active, completed, cancelled);
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
                ? "Tu reserva en "
                  + reservation.getProperty().getTitle()
                  + " fue completada. Se retuvo $"
                  + retainedAmount.setScale(2, RoundingMode.HALF_UP)
                  + " del depósito de garantía por daños y se reembolsó $"
                  + refundAmount.setScale(2, RoundingMode.HALF_UP)
                  + "."
                : "Tu reserva en "
                  + reservation.getProperty().getTitle()
                  + " fue completada. El depósito de garantía fue reembolsado completamente.";

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

    @Override
    public Page<ReservationResponse> getAllSystemReservations(int page, int pageSize, String sortBy, String sortOrder, ReservationStatus status, String searchTerm) {
        int safePage = Math.max(page, 0);
        int safePageSize = Math.clamp(pageSize, 1, 100);

        Pageable pageable = PaginationUtils.getPageRequest(safePage, safePageSize, sortBy, sortOrder);
        String normalizedSearchTerm = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.trim();

        Page<Reservation> reservationsPage = reservationRepository.findAllSystemReservationsWithFilters(
                status,
                normalizedSearchTerm,
                pageable
        );

        return reservationsPage.map(ReservationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ReservationCancellationPreviewResponse previewCancellation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        validateCancellationPermission(currentUser, reservation);
        validateReservationCanBeCancelled(reservation);

        return buildCancellationPreview(reservation, currentUser);
    }

    private ReservationCancellationPreviewResponse buildCancellationPreview(
            Reservation reservation,
            AppUser currentUser
    ) {
        LocalDate today = LocalDate.now();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, reservation.getCheckInDate());

        BigDecimal cancellationPenalty = calculateCancellationPenalty(
                reservation,
                daysUntilCheckIn,
                currentUser
        );

        BigDecimal reservationRefundAmount = calculateReservationRefundAmount(reservation, cancellationPenalty);
        BigDecimal cleaningFeeRefundAmount = reservation.getCleaningFee();
        BigDecimal guaranteeDepositRefundAmount = getGuaranteeDepositAmount(reservation);

        BigDecimal totalRefundAmount = reservationRefundAmount
                .add(cleaningFeeRefundAmount)
                .add(guaranteeDepositRefundAmount);

        return new ReservationCancellationPreviewResponse(
                reservation.getId(),
                reservation.getReservationStatus(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                daysUntilCheckIn,
                cancellationPenalty,
                reservationRefundAmount,
                cleaningFeeRefundAmount,
                guaranteeDepositRefundAmount,
                totalRefundAmount
        );
    }

    private BigDecimal getGuaranteeDepositAmount(Reservation reservation) {
        return paymentRepository.findByReservationAndPaymentType(reservation, PaymentType.GUARANTEE_DEPOSIT)
                .map(Payment::getAmount)
                .orElse(BigDecimal.ZERO);
    }
}