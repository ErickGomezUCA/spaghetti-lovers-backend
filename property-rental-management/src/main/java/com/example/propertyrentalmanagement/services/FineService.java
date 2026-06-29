package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateFineRequest;
import com.example.propertyrentalmanagement.dto.request.PayFineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;
import com.example.propertyrentalmanagement.dto.response.FineSummaryResponse;
import com.example.propertyrentalmanagement.dto.response.FineSummaryStatsResponse;
import com.example.propertyrentalmanagement.enums.FineType;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface FineService {
    FineResponse createFine(CreateFineRequest fineRequest);

    FineResponse payFine(UUID fineId, PayFineRequest fineRequest);

    Page<FineSummaryResponse> getLandlordFines(
            int page, int pageSize, String sortBy, String sortOrder,
            FineType fineType, Boolean resolved, String searchTerm
    );

    FineSummaryStatsResponse getLandlordFinesSummary();

    Page<FineSummaryResponse> getMyFines(int page, int pageSize, String sortBy, String sortOrder);
}
