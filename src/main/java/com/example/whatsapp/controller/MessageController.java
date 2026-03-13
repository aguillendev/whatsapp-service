package com.example.whatsapp.controller;

import com.example.whatsapp.service.WhatsappService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Mensajes", description = "Endpoints para enviar mensajes de WhatsApp")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final WhatsappService whatsappService;

    public MessageController(WhatsappService whatsappService) {
        this.whatsappService = whatsappService;
    }

    @Operation(
            summary = "Enviar mensaje de texto",
            description = "Envía un mensaje de texto simple a un número de WhatsApp.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SendTextRequest.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo",
                                    value = """
                                            {
                                              "to": "5491112345678",
                                              "message": "Hola! Este es un mensaje de prueba."
                                            }"""
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mensaje enviado correctamente"),
                    @ApiResponse(responseCode = "500", description = "Error al enviar el mensaje")
            }
    )
    @PostMapping("/text")
    public ResponseEntity<String> sendText(@RequestBody SendTextRequest request) {
        log.info("Enviando mensaje de texto a: {}", request.to());
        whatsappService.sendTextMessage(request.to(), request.message());
        return ResponseEntity.ok("Mensaje enviado correctamente a " + request.to());
    }

    @Operation(
            summary = "Enviar mensaje con template",
            description = "Envía un mensaje usando una plantilla aprobada de WhatsApp Business. " +
                    "El template debe existir en tu cuenta de Meta Business.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SendTemplateRequest.class),
                            examples = @ExampleObject(
                                    name = "hello_world",
                                    value = """
                                            {
                                              "to": "5491112345678",
                                              "templateName": "hello_world",
                                              "languageCode": "en_US"
                                            }"""
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template enviado correctamente"),
                    @ApiResponse(responseCode = "500", description = "Error al enviar el template")
            }
    )
    @PostMapping("/template")
    public ResponseEntity<String> sendTemplate(@RequestBody SendTemplateRequest request) {
        log.info("Enviando template '{}' a: {}", request.templateName(), request.to());
        whatsappService.sendTemplateMessage(request.to(), request.templateName(), request.languageCode());
        return ResponseEntity.ok("Template '" + request.templateName() + "' enviado a " + request.to());
    }

    // ── DTOs de request ────────────────────────────────────────────────────────

    @Schema(description = "Datos para enviar un mensaje de texto")
    public record SendTextRequest(
            @Schema(description = "Número de teléfono del destinatario con código de país (sin +)", example = "5491112345678")
            String to,
            @Schema(description = "Contenido del mensaje de texto", example = "Hola! Este es un mensaje de prueba.")
            String message
    ) {}

    @Schema(description = "Datos para enviar un mensaje con template")
    public record SendTemplateRequest(
            @Schema(description = "Número de teléfono del destinatario con código de país (sin +)", example = "5491112345678")
            String to,
            @Schema(description = "Nombre del template aprobado en Meta Business", example = "hello_world")
            String templateName,
            @Schema(description = "Código de idioma del template", example = "en_US")
            String languageCode
    ) {}
}
