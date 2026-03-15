package com.example.whatsapp.service;

import com.example.whatsapp.config.WhatsappProperties;
import com.example.whatsapp.dto.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappService.class);

    private final RestClient restClient;
    private final WhatsappProperties properties;

    public WhatsappService(RestClient whatsappRestClient, WhatsappProperties properties) {
        this.restClient = whatsappRestClient;
        this.properties = properties;
    }

    public void sendTextMessage(String to, String message) {
        MessageRequest request = MessageRequest.createText(normalizePhoneNumber(to), message);
        sendMessage(request);
    }

    public void sendTemplateMessage(String to, String templateName, String languageCode) {
        MessageRequest request = MessageRequest.createTemplate(normalizePhoneNumber(to), templateName, languageCode);
        sendMessage(request);
    }

    /**
     * Normaliza números argentinos eliminando el '9' entre el código de país (54)
     * y el número de área: 549XXXXXXXXXX → 54XXXXXXXXXX.
     * Solo aplica si la propiedad 'whatsapp.normalize-argentine-numbers' está en true.
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (!properties.normalizeArgentineNumbers()) {
            return phoneNumber;
        }
        // Formato argentino vía WhatsApp: 549 + 10 dígitos = 13 dígitos en total
        if (phoneNumber != null && phoneNumber.startsWith("549") && phoneNumber.length() == 13) {
            String normalized = "54" + phoneNumber.substring(3);
            log.debug("Número normalizado: {} → {}", phoneNumber, normalized);
            return normalized;
        }
        return phoneNumber;
    }

    private void sendMessage(MessageRequest request) {
        String uri = "/" + properties.phoneNumberId() + "/messages";
        log.info("Enviando mensaje a {} - tipo: {}", request.to(), request.type());
        try {
            restClient.post()
                    .uri(uri)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Mensaje enviado exitosamente a {}", request.to());
        } catch (Exception e) {
            log.error("Error al enviar mensaje a {}: {}", request.to(), e.getMessage());
            throw e;
        }
    }
}