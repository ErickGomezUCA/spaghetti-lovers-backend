package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.response.AccessCodeDetailResponse;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.services.AccessCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-codes")
@RequiredArgsConstructor
public class AccessCodeController {

    private final AccessCodeService accessCodeService;

    @PreAuthorize("@authorizationService.isTenant()")
    @GetMapping("/tenant")
    public ResponseEntity<GenericResponse> getTenantAccessCodes() {
        List<AccessCodeDetailResponse> accessCodes = accessCodeService.getTenantAccessCodes();

        return GenericResponse.builder()
                .message("Tenant access codes retrieved successfully")
                .data(accessCodes)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }

    @PreAuthorize("@authorizationService.isLandlord()")
    @GetMapping("/landlord")
    public ResponseEntity<GenericResponse> getLandlordAccessCodes() {
        List<AccessCodeDetailResponse> accessCodes = accessCodeService.getLandlordAccessCodes();

        return GenericResponse.builder()
                .message("Landlord access codes retrieved successfully")
                .data(accessCodes)
                .status(HttpStatus.OK)
                .build()
                .buildResponse();
    }
}