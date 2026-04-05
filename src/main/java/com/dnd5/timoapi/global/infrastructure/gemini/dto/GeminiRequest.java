package com.dnd5.timoapi.global.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeminiRequest(
        @JsonProperty("system_instruction") SystemInstruction systemInstruction,
        List<Content> contents
) {
    public static GeminiRequest of(String systemPrompt, String userPrompt) {
        SystemInstruction systemInstruction = new SystemInstruction(List.of(new Part(systemPrompt)));
        Content userContent = new Content("user", List.of(new Part(userPrompt)));

        return new GeminiRequest(systemInstruction, List.of(userContent));
    }

    public record SystemInstruction(List<Part> parts) {}
    public record Content(String role, List<Part> parts) {}
    public record Part(String text) {}
}
