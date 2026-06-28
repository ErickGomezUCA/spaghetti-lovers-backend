package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.AccessCodeDetailResponse;
import com.example.propertyrentalmanagement.dto.response.AccessCodeResponse;
import com.example.propertyrentalmanagement.entitites.Reservation;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AccessCodeService {

    AccessCodeResponse generateAccessCodeForReservation(Reservation reservation);

    AccessCodeResponse getActiveAccessCodeByReservationId(UUID reservationId);

    Page<AccessCodeDetailResponse> getTenantAccessCodes(
            int page,
            int pageSize,
            String sortBy,
            String sortOrder
    );

    Page<AccessCodeDetailResponse> getLandlordAccessCodes(
            int page,
            int pageSize,
            String sortBy,
            String sortOrder
    );

    void invalidateCodesByReservation(Reservation reservation);

    void extendAccessCodesValidUntil(Reservation reservation, LocalDateTime newValidUntil);
}
