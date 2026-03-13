package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TextMessage(
        String body,
        @JsonProperty("preview_url")
        Boolean previewUrl
) {
}