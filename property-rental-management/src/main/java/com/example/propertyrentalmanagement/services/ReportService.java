package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.response.PropertyReportResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportService {
    PropertyReportResponse getPropertyReport(UUID propertyId, LocalDate startDate, LocalDate endDate);
}