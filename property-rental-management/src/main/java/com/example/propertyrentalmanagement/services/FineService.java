package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateFineRequest;
import com.example.propertyrentalmanagement.dto.request.PayFineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;

import java.util.UUID;

public interface FineService {

    FineResponse createFine(CreateFineRequest fineRequest);
    FineResponse payFine(UUID fineId, PayFineRequest fineRequest);}
