package com.example.whatsapp.dto;

public record Message(
        String from,
        String id,
        String timestamp,
        String type,
        Text text,
        Audio audio
) {
    public record Text(
            String body
    ) {}

    public record Audio(
            String id,
            String mimeType
    ) {}
}