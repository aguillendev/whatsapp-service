package com.example.whatsapp.controller;

import com.example.whatsapp.config.WhatsappProperties;
import com.example.whatsapp.dto.Message;
import com.example.whatsapp.dto.WebhookPayload;
import com.example.whatsapp.security.SignatureValidator;
import com.example.whatsapp.service.AudioTranscriptionService;
import com.example.whatsapp.service.McpClientService;
import com.example.whatsapp.service.WhatsappService;
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
    private final McpClientService mcpClientService;
    private final WhatsappService whatsappService;
    private final AudioTranscriptionService audioTranscriptionService;

    public WebhookController(WhatsappProperties properties,
                             SignatureValidator signatureValidator,
                             ObjectMapper objectMapper,
                             McpClientService mcpClientService,
                             WhatsappService whatsappService,
                             AudioTranscriptionService audioTranscriptionService) {
        this.properties = properties;
        this.signatureValidator = signatureValidator;
        this.objectMapper = objectMapper;
        this.mcpClientService = mcpClientService;
        this.whatsappService = whatsappService;
        this.audioTranscriptionService = audioTranscriptionService;
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
            description = "Meta envía a este endpoint los mensajes entrantes. Valida la firma HMAC-SHA256, "
                    + "extrae el texto del mensaje, lo procesa con el LLM (mcp-client) y responde al usuario.",
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
            log.warn("[DEV MODE] Validación de firma omitida. Configurá 'app-secret' en application.yml para habilitar la seguridad.");
        } else if (!signatureValidator.isValidSignature(rawPayload, signatureHeader)) {
            log.error("Firma inválida. Header recibido: {}", signatureHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        log.info("Webhook recibido: {}", rawPayload);

        try {
            WebhookPayload payload = objectMapper.readValue(rawPayload, WebhookPayload.class);

            // Recorrer entries y changes para detectar mensajes de texto entrantes
            if (payload.entry() != null) {
                for (var entry : payload.entry()) {
                    if (entry.changes() == null) continue;
                    for (var change : entry.changes()) {
                        if (change.value() == null || change.value().messages() == null) continue;
                        for (Message message : change.value().messages()) {
                            String from = message.from();
                            String userText = null;

                            if ("text".equals(message.type()) && message.text() != null) {
                                userText = message.text().body();
                                log.info("Mensaje de texto recibido de {}: {}", from, userText);

                            } else if ("audio".equals(message.type()) && message.audio() != null) {
                                String mediaId = message.audio().id();
                                log.info("Mensaje de audio recibido de {}, mediaId={}", from, mediaId);
                                userText = audioTranscriptionService.transcribe(mediaId);
                                if (userText == null) {
                                    log.warn("No se pudo transcribir el audio de {}", from);
                                    whatsappService.sendTextMessage(from,
                                            "Lo siento, no pude entender el audio. ¿Podés repetirlo o escribirme?");
                                    continue;
                                }
                                log.info("Audio transcripto de {}: {}", from, userText);

                            } else {
                                log.info("Evento ignorado (tipo no soportado): tipo={}", message.type());
                                continue;
                            }

                            // Procesar con el LLM via mcp-client (el from es el ID de sesión)
                            String aiResponse = mcpClientService.processMessage(from, userText);

                            // Responder al usuario por WhatsApp
                            whatsappService.sendTextMessage(from, aiResponse);
                        }
                    }
                }
            }

            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (JsonProcessingException e) {
            log.error("Error al parsear payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload");
        }
    }
}