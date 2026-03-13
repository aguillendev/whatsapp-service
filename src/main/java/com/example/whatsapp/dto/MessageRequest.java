package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageRequest(
        @JsonProperty("messaging_product")
        String messagingProduct,
        String to,
        String type,
        TextMessage text,
        TemplateMessage template
) {
    public static MessageRequest createText(String to, String body) {
        return new MessageRequest(
                "whatsapp",
                to,
                "text",
                new TextMessage(body, false),
                null
        );
    }

    public static MessageRequest createTemplate(String to, String templateName, String languageCode) {
        return new MessageRequest(
                "whatsapp",
                to,
                "template",
                null,
                new TemplateMessage(templateName, new TemplateLanguage(languageCode))
        );
    }
}