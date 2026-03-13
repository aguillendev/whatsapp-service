package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Contact(
        Profile profile,
        @JsonProperty("wa_id")
        String waId
) {
    public record Profile(
            String name
    ) {}
}