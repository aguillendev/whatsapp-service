package com.example.whatsapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP que se comunica con el mcp-client (puerto 8082).
 * Envía el mensaje del usuario junto con su número de teléfono (ID de sesión)
 * para que el LLM mantenga el historial de conversación.
 */
@Service
public class McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientService.class);

    private final RestClient mcpRestClient;

    public McpClientService(RestClient mcpRestClient) {
        this.mcpRestClient = mcpRestClient;
    }

    /**
     * Envía un mensaje al mcp-client y devuelve la respuesta del LLM.
     *
     * @param phoneNumber El número de teléfono del usuario (actúa como ID de sesión)
     * @param message     El texto del mensaje del usuario
     * @return La respuesta generada por el LLM
     */
    public String processMessage(String phoneNumber, String message) {
        log.info("Enviando mensaje al mcp-client [sesión={}]: {}", phoneNumber, message);
        try {
            String response = mcpRestClient.post()
                    .uri("/api/chat")
                    .body(new ChatRequest(phoneNumber, message))
                    .retrieve()
                    .body(String.class);
            log.info("Respuesta recibida del mcp-client [sesión={}]: {}", phoneNumber, response);
            return response;
        } catch (Exception e) {
            log.error("Error al comunicarse con el mcp-client [sesión={}]: {}", phoneNumber, e.getMessage());
            return "Lo siento, no pude procesar tu consulta en este momento. Por favor intentá de nuevo más tarde.";
        }
    }

    private record ChatRequest(String phoneNumber, String message) {}
}
