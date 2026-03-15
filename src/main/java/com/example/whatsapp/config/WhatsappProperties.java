package com.example.whatsapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp")
public record WhatsappProperties(
        String accessToken,
        String phoneNumberId,
        String verifyToken,
        String appSecret,
        String apiVersion,
        boolean normalizeArgentineNumbers
) {
}