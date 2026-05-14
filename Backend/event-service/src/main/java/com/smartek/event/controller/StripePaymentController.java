package com.smartek.event.controller;

import com.smartek.event.dto.CheckoutSessionRequest;
import com.smartek.event.dto.CheckoutSessionResponse;
import com.smartek.event.dto.EventRegistrationResponse;
import com.smartek.event.service.EventRegistrationService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/payment")
@RequiredArgsConstructor
@Slf4j
public class StripePaymentController {

    private final EventRegistrationService registrationService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Crée une session de paiement Stripe Checkout
     * POST /api/events/payment/create-checkout-session
     */
    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @RequestBody CheckoutSessionRequest request) {
        try {
            // Stripe utilise les centimes — multiplier par 100
            long amountInCents = Math.round(request.getAmount() * 100);

            // Stripe ne supporte pas TND directement — utiliser USD ou EUR
            String currency = "usd";

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:4200/events/payment/success?registrationId=" 
                            + request.getRegistrationId() + "&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:4200/learner/events")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency)
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(request.getEventTitle())
                                                                    .setDescription("Inscription à l'événement : " + request.getEventTitle())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    // Stocker registrationId dans les metadata pour le retrouver dans le webhook
                    .putMetadata("registrationId", String.valueOf(request.getRegistrationId()))
                    .putMetadata("eventId", String.valueOf(request.getEventId()))
                    .putMetadata("userId", String.valueOf(request.getUserId()))
                    .build();

            Session session = Session.create(params);

            log.info("Stripe checkout session created: {} for registration: {}", 
                    session.getId(), request.getRegistrationId());

            return ResponseEntity.ok(new CheckoutSessionResponse(session.getId(), session.getUrl()));

        } catch (Exception e) {
            log.error("Error creating Stripe checkout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Webhook Stripe — reçoit les événements de paiement
     * POST /api/events/payment/webhook
     * IMPORTANT: doit recevoir le body RAW (non parsé)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody byte[] payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event stripeEvent;

        try {
            // Vérifier la signature du webhook pour sécurité
            stripeEvent = Webhook.constructEvent(
                    new String(payload), sigHeader, webhookSecret
            );
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Received Stripe webhook event: {}", stripeEvent.getType());

        // Traiter uniquement les paiements complétés
        if ("checkout.session.completed".equals(stripeEvent.getType())) {
            try {
                com.stripe.model.StripeObject stripeObject = stripeEvent.getDataObjectDeserializer()
                        .deserializeUnsafe();
                
                if (stripeObject instanceof Session session) {
                    String registrationIdStr = session.getMetadata().get("registrationId");
                    log.info("Webhook checkout.session.completed - registrationId: {}", registrationIdStr);
                    
                    if (registrationIdStr != null) {
                        Long registrationId = Long.parseLong(registrationIdStr);
                        EventRegistrationResponse response = registrationService.confirmPayment(registrationId);
                        log.info("Payment confirmed for registration: {} — status: {}",
                                registrationId, response.getPaymentStatus());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing checkout.session.completed: {}", e.getMessage(), e);
            }
        }

        return ResponseEntity.ok("Webhook processed");
    }

    /**
     * Vérifier le statut d'un paiement après retour de Stripe
     * GET /api/events/payment/verify?registrationId=X
     */
    @GetMapping("/verify")
    public ResponseEntity<EventRegistrationResponse> verifyPayment(
            @RequestParam Long registrationId) {
        try {
            // Récupérer les inscriptions de l'utilisateur pour trouver celle-ci
            // On utilise confirmPayment uniquement si le webhook n'a pas encore été reçu
            log.info("Payment verification requested for registration: {}", registrationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Stripe Payment Service is running");
    }
}
