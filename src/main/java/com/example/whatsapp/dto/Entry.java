package com.example.whatsapp.dto;

import java.util.List;

public record Entry(
        String id,
        List<Change> changes
) {
}