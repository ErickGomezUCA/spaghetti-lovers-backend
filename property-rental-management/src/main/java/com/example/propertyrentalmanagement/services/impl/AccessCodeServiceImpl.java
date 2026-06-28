package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.response.AccessCodeResponse;
import com.example.propertyrentalmanagement.entitites.AccessCode;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.CodeType;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.AccessCodeNotFoundException;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.ReservationNotFoundException;
import com.example.propertyrentalmanagement.repositories.AccessCodeRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.propertyrentalmanagement.dto.response.AccessCodeDetailResponse;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessCodeServiceImpl implements AccessCodeService {

    private static final String CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final AccessCodeRepository accessCodeRepository;
    private final ReservationRepository reservationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AccessCodeResponse generateAccessCodeForReservation(Reservation reservation) {
        AccessCode accessCode = AccessCode.builder()
                .property(reservation.getProperty())
                .reservation(reservation)
                .code(generateCode())
                .codeType(CodeType.ACCESS_CODE)
                .validFrom(reservation.getCheckInDate().atStartOfDay())
                .validUntil(reservation.getCheckOutDate().atTime(LocalTime.MAX))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        AccessCode savedAccessCode = accessCodeRepository.save(accessCode);

        return AccessCodeResponse.fromEntity(savedAccessCode);
    }

    @Override
    public AccessCodeResponse getActiveAccessCodeByReservationId(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        validateReservationAccess(currentUser, reservation);

        AccessCode accessCode = accessCodeRepository
                .findByReservationAndIsActiveTrueAndCodeType(reservation, CodeType.ACCESS_CODE)
                .orElseThrow(() -> new AccessCodeNotFoundException("Active access code not found"));

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(accessCode.getValidFrom()) || now.isAfter(accessCode.getValidUntil())) {
            throw new IllegalStateException("Access code is not currently valid");
        }

        return AccessCodeResponse.fromEntity(accessCode);
    }

    @Override
    public void invalidateCodesByReservation(Reservation reservation) {
        List<AccessCode> accessCodes = accessCodeRepository.findByReservation(reservation);

        accessCodes.forEach(accessCode -> accessCode.setIsActive(false));

        accessCodeRepository.saveAll(accessCodes);
    }

    @Override
    public void extendAccessCodesValidUntil(Reservation reservation, LocalDateTime newValidUntil) {
        accessCodeRepository.findByReservationAndIsActiveTrueAndCodeType(reservation, CodeType.ACCESS_CODE)
                .ifPresent(accessCode -> {
                    accessCode.setValidUntil(newValidUntil);
                    accessCodeRepository.save(accessCode);
                });
    }

    private void validateReservationAccess(AppUser currentUser, Reservation reservation) {
        boolean isTenantOwner = reservation.getTenant().getId().equals(currentUser.getId());
        boolean isPropertyLandlord = reservation.getProperty().getLandlord().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isTenantOwner && !isPropertyLandlord && !isAdmin) {
            throw new NotResourceOwnerException("You are not allowed to access this reservation code");
        }
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(CODE_CHARACTERS.length());
            code.append(CODE_CHARACTERS.charAt(index));
        }

        return code.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccessCodeDetailResponse> getTenantAccessCodes(
            int page,
            int pageSize,
            String sortBy,
            String sortOrder
    ) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        if (currentUser.getRole() != UserRole.TENANT) {
            throw new NotResourceOwnerException("Only tenants can access tenant access codes");
        }

        Pageable pageable = PaginationUtils.getPageRequest(
                page,
                pageSize,
                sortBy,
                sortOrder
        );

        return accessCodeRepository
                .findByReservation_Tenant_IdAndCodeType(
                        currentUser.getId(),
                        CodeType.ACCESS_CODE,
                        pageable
                )
                .map(AccessCodeDetailResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccessCodeDetailResponse> getLandlordAccessCodes(
            int page,
            int pageSize,
            String sortBy,
            String sortOrder
    ) {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();

        if (currentUser.getRole() != UserRole.LANDLORD) {
            throw new NotResourceOwnerException("Only landlords can access landlord access codes");
        }

        Pageable pageable = PaginationUtils.getPageRequest(
                page,
                pageSize,
                sortBy,
                sortOrder
        );

        return accessCodeRepository
                .findByReservation_Property_Landlord_IdAndCodeType(
                        currentUser.getId(),
                        CodeType.ACCESS_CODE,
                        pageable
                )
                .map(AccessCodeDetailResponse::fromEntity);
    }
}
