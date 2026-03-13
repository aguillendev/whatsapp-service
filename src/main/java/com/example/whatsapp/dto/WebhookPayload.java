package com.example.whatsapp.dto;

import java.util.List;

public record WebhookPayload(
        String object,
        List<Entry> entry
) {
}