package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateFineRequest;
import com.example.propertyrentalmanagement.dto.request.PayFineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;
import com.example.propertyrentalmanagement.dto.response.FineSummaryResponse;
import com.example.propertyrentalmanagement.dto.response.FineSummaryStatsResponse;
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
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

            if (type == FineType.NOISE_VIOLATION) {
                boolean isOccupiedRightNow = reservationRepository.isPropertyOccupiedToday(reservation.getProperty().getId(), java.time.LocalDate.now());
                if (!isOccupiedRightNow) {
                    throw new BadRequestException("Cannot issue a noise violation: the tenant is not physically occupying the property today.");
                }
            } else {
                LocalDateTime checkoutLimit = reservation.getCheckOutDate().atTime(11, 0);
                if (LocalDateTime.now().isBefore(checkoutLimit)) {
                    throw new BadRequestException("Cannot issue a late checkout fine. The tenant still has time until " + checkoutLimit.toLocalTime() + ".");
                }
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
        if (request.paymentMethod() == PaymentMethod.PENDING) {
            throw new BadRequestException("A valid (non-pending) payment method is required.");
        }
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

    @Override
    public Page<FineSummaryResponse> getLandlordFines(
            int page, int pageSize, String sortBy, String sortOrder,
            FineType fineType, Boolean resolved, String searchTerm
    ) {
        int safePage = Math.max(page, 0);
        int safePageSize = Math.clamp(pageSize, 1, 100);
        Pageable pageable = PaginationUtils.getPageRequest(safePage, safePageSize, sortBy, sortOrder);
        String normalizedSearch = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.trim();
        UUID landlordId = authProvider.getCurrentUser().getId();

        Page<Fine> finesPage = fineRepository.findLandlordFinesWithFilters(
                landlordId, fineType, resolved, normalizedSearch, pageable);
        return finesPage.map(FineSummaryResponse::fromEntity);
    }

    @Override
    public FineSummaryStatsResponse getLandlordFinesSummary() {
        UUID landlordId = authProvider.getCurrentUser().getId();

        long total = fineRepository.countByReservation_Property_LandlordId(landlordId);
        long pendingCount = fineRepository.countByReservation_Property_LandlordIdAndResolvedAtIsNull(landlordId);
        BigDecimal pendingAmount = fineRepository.sumPendingAmountByLandlord(landlordId);
        BigDecimal resolvedAmount = fineRepository.sumResolvedAmountByLandlord(landlordId);

        return new FineSummaryStatsResponse(total, pendingCount, pendingAmount, resolvedAmount);
    }

    @Override
    public Page<FineSummaryResponse> getMyFines(int page, int pageSize, String sortBy, String sortOrder) {
        UUID tenantId = authProvider.getCurrentUser().getId();
        int safePage = Math.max(page, 0);
        int safePageSize = Math.clamp(pageSize, 1, 100);

        Pageable pageable = PaginationUtils.getPageRequest(safePage, safePageSize, sortBy, sortOrder);
        Page<Fine> finesPage = fineRepository.findByReservation_Tenant_Id(tenantId, pageable);

        return finesPage.map(FineSummaryResponse::fromEntity);
    }
}