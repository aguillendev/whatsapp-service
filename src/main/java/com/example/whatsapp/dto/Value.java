package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Value(
        @JsonProperty("messaging_product")
        String messagingProduct,
        Metadata metadata,
        List<Contact> contacts,
        List<Message> messages
) {
    public record Metadata(
            @JsonProperty("display_phone_number")
            String displayPhoneNumber,
            @JsonProperty("phone_number_id")
            String phoneNumberId
    ) {}
}