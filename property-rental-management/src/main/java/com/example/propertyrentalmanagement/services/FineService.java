package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.FineRequest;
import com.example.propertyrentalmanagement.dto.response.FineResponse;

public interface FineService {

    FineResponse createFine(FineRequest request, String currentUsername, boolean isAdmin);
    //FineResponse payFine(FineRequest request, String currentUsername, boolean isAdmin);
}
