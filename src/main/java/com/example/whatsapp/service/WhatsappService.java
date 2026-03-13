package com.example.whatsapp.service;

import com.example.whatsapp.config.WhatsappProperties;
import com.example.whatsapp.dto.MessageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WhatsappService {

    private final RestClient restClient;
    private final WhatsappProperties properties;

    public WhatsappService(RestClient whatsappRestClient, WhatsappProperties properties) {
        this.restClient = whatsappRestClient;
        this.properties = properties;
    }

    public void sendTextMessage(String to, String message) {
        MessageRequest request = MessageRequest.createText(to, message);
        sendMessage(request);
    }

    public void sendTemplateMessage(String to, String templateName, String languageCode) {
        MessageRequest request = MessageRequest.createTemplate(to, templateName, languageCode);
        sendMessage(request);
    }

    private void sendMessage(MessageRequest request) {
        String uri = "/" + properties.phoneNumberId() + "/messages";
        restClient.post()
                .uri(uri)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}