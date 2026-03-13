package com.example.whatsapp.controller;

import com.example.whatsapp.config.WhatsappProperties;
import com.example.whatsapp.dto.WebhookPayload;
import com.example.whatsapp.security.SignatureValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WhatsappProperties properties;
    private final SignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    public WebhookController(WhatsappProperties properties, SignatureValidator signatureValidator, ObjectMapper objectMapper) {
        this.properties = properties;
        this.signatureValidator = signatureValidator;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && properties.verifyToken().equals(token)) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
    }

    @PostMapping
    public ResponseEntity<String> receiveMessage(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader) {

        if (!signatureValidator.isValidSignature(rawPayload, signatureHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        try {
            WebhookPayload payload = objectMapper.readValue(rawPayload, WebhookPayload.class);

            // Process the payload here...
            System.out.println("Received payload: " + payload);

            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload");
        }
    }
}