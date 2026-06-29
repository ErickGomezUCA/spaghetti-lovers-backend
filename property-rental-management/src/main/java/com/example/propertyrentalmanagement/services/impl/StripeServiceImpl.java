package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreatePaymentIntentRequest;
import com.example.propertyrentalmanagement.dto.response.StripePaymentIntentResponse;
import com.example.propertyrentalmanagement.entitites.Payment;
import com.example.propertyrentalmanagement.enums.PaymentMethod;
import com.example.propertyrentalmanagement.exceptions.BadRequestException;
import com.example.propertyrentalmanagement.exceptions.PaymentNotFoundException;
import com.example.propertyrentalmanagement.exceptions.StripePaymentException;
import com.example.propertyrentalmanagement.repositories.PaymentRepository;
import com.example.propertyrentalmanagement.services.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private final PaymentRepository paymentRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional
    public StripePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        Payment payment = paymentRepository.findById(request.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + request.paymentId()));

        if (payment.getStripePaymentIntentId() != null) {
            throw new BadRequestException("A Stripe PaymentIntent already exists for this payment.");
        }

        long amountInCents = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .addPaymentMethodType("card")
                    .putMetadata("paymentId", payment.getId().toString())
                    .putMetadata("reservationId", payment.getReservation().getId().toString())
                    .putMetadata("paymentType", payment.getPaymentType().name())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            payment.setStripePaymentIntentId(intent.getId());
            payment.setPaymentMethod(PaymentMethod.CARD);
            paymentRepository.save(payment);

            return new StripePaymentIntentResponse(
                    intent.getClientSecret(),
                    intent.getId(),
                    payment.getAmount(),
                    "usd"
            );
        } catch (StripeException e) {
            throw new StripePaymentException("Failed to create Stripe PaymentIntent: " + e.getMessage(), e);
        }
    }

    @Override
    public void handleWebhookEvent(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            throw new StripePaymentException("Invalid Stripe webhook signature.", e);
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(stripeObject -> {
                PaymentIntent intent = (PaymentIntent) stripeObject;
                paymentRepository.findByStripePaymentIntentId(intent.getId())
                        .ifPresent(payment -> {
                        });
            });
        }
    }
}
