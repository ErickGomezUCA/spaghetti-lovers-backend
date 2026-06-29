package com.example.propertyrentalmanagement.controllers;

import com.example.propertyrentalmanagement.dto.request.CreatePaymentIntentRequest;
import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.dto.response.StripePaymentIntentResponse;
import com.example.propertyrentalmanagement.services.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/payment-intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {

        StripePaymentIntentResponse response = stripeService.createPaymentIntent(request);

        return GenericResponse.builder()
                .message("PaymentIntent created successfully")
                .data(response)
                .status(HttpStatus.CREATED)
                .build().buildResponse();
    }

    @PostMapping("/webhook")
    public ResponseEntity<GenericResponse> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        stripeService.handleWebhookEvent(payload, sigHeader);

        return GenericResponse.builder()
                .message("Webhook received")
                .status(HttpStatus.OK)
                .build().buildResponse();
    }
}
