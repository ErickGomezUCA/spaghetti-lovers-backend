package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.response.AccessCodeDetailResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.propertyrentalmanagement.dto.response.PaginationMeta;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/access-codes")
@RequiredArgsConstructor
public class AccessCodeController {

    private final AccessCodeService accessCodeService;

    @PreAuthorize("@authorizationService.isTenant()")
    @GetMapping("/tenant")
    public ResponseEntity<GenericResponse> getTenantAccessCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "validFrom") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<AccessCodeDetailResponse> accessCodes =
                accessCodeService.getTenantAccessCodes(
                        page,
                        pageSize,
                        sortBy,
                        sortOrder
                );

        return GenericResponse.builder()
                .message("Tenant access codes retrieved successfully")
                .data(accessCodes.getContent())
                .pagination(PaginationMeta.fromPage(accessCodes))
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @GetMapping("/landlord")
    public ResponseEntity<GenericResponse> getLandlordAccessCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "validFrom") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<AccessCodeDetailResponse> accessCodes =
                accessCodeService.getLandlordAccessCodes(
                        page,
                        pageSize,
                        sortBy,
                        sortOrder
                );

        return GenericResponse.builder()
                .message("Landlord access codes retrieved successfully")
                .data(accessCodes.getContent())
                .pagination(PaginationMeta.fromPage(accessCodes))
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }
}