package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.PropertyReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportService {
    PropertyReportResponse getPropertyReport(UUID propertyId, LocalDate startDate, LocalDate endDate);
    List<PropertyReportResponse> getAllPropertiesReport(LocalDate startDate, LocalDate endDate, UUID landlordId);
}