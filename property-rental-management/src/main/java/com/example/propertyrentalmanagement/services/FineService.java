package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreateFineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;

public interface FineService {

    FineResponse createFine(CreateFineRequest fineRequest);
    //FineResponse payFine(FineRequest request);
}
