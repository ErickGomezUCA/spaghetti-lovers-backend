package com.example.propertyrentalmanagement.services;

import com.example.propertyrentalmanagement.dto.request.CreatePaymentIntentRequest;
import com.example.propertyrentalmanagement.dto.response.StripePaymentIntentResponse;

public interface StripeService {
    StripePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request);
    void handleWebhookEvent(String payload, String sigHeader);
}
