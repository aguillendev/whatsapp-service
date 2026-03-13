package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Message(
        String from,
        String id,
        String timestamp,
        String type,
        Text text
) {
    public record Text(
            String body
    ) {}
}