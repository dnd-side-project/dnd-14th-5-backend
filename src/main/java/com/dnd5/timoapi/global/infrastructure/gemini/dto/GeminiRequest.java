package com.dnd5.timoapi.global.infrastructure.gemini.dto;

import java.util.List;

public record GeminiRequest(
        List<Content> contents
) {
    public static GeminiRequest of(String text) {
        return new GeminiRequest(
                List.of(new Content("user", List.of(new Part(text))))
        );
    }

    public record Content(String role, List<Part> parts) {}
    public record Part(String text) {}
}
