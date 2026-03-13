package com.example.whatsapp.controller;

import com.example.whatsapp.config.WhatsappProperties;
import com.example.whatsapp.dto.WebhookPayload;
import com.example.whatsapp.security.SignatureValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@Tag(name = "Webhook", description = "Endpoints de verificación y recepción de mensajes de WhatsApp via Meta Webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private static final String PLACEHOLDER_SECRET = "playmatch";

    private final WhatsappProperties properties;
    private final SignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    public WebhookController(WhatsappProperties properties, SignatureValidator signatureValidator,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.signatureValidator = signatureValidator;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Verificar webhook",
            description = "Meta llama a este endpoint con GET para verificar que la URL del webhook es válida. "
                    + "Compara el token recibido con el 'verify-token' configurado en application.yml.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token válido, webhook verificado"),
                    @ApiResponse(responseCode = "403", description = "Token inválido")
            }
    )
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

    @Operation(
            summary = "Recibir mensaje entrante",
            description = "Meta envía a este endpoint los mensajes entrantes, actualizaciones de estado y otros eventos. "
                    + "Valida la firma HMAC-SHA256 del payload para garantizar que el origen es Meta.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evento recibido y procesado"),
                    @ApiResponse(responseCode = "401", description = "Firma inválida"),
                    @ApiResponse(responseCode = "400", description = "Payload JSON inválido")
            }
    )
    @PostMapping
    public ResponseEntity<String> receiveMessage(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader) {

        boolean devMode = PLACEHOLDER_SECRET.equals(properties.appSecret());

        if (devMode) {
            log.warn(
                    "[DEV MODE] Validación de firma omitida. Configurá 'app-secret' en application.yml para habilitar la seguridad.");
        } else if (!signatureValidator.isValidSignature(rawPayload, signatureHeader)) {
            log.error("Firma inválida. Header recibido: {}", signatureHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        log.info("Webhook recibido: {}", rawPayload);

        try {
            WebhookPayload payload = objectMapper.readValue(rawPayload, WebhookPayload.class);
            log.info("Payload procesado: {}", payload);

            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (JsonProcessingException e) {
            log.error("Error al parsear payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload");
        }
    }
}