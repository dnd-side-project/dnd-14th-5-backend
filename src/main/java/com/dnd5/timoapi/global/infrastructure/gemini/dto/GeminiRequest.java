package com.dnd5.timoapi.global.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeminiRequest(
        @JsonProperty("system_instruction") SystemInstruction systemInstruction,
        List<Content> contents,
        @JsonProperty("generationConfig") GenerationConfig generationConfig
) {
    public static GeminiRequest of(String systemPrompt, String userPrompt) {
        return of(systemPrompt, userPrompt, "application/json");
    }

    public static GeminiRequest ofPlainText(String systemPrompt, String userPrompt) {
        return of(systemPrompt, userPrompt, "text/plain");
    }

    private static GeminiRequest of(String systemPrompt, String userPrompt, String responseMimeType) {
        SystemInstruction systemInstruction = new SystemInstruction(List.of(new Part(systemPrompt)));
        Content userContent = new Content("user", List.of(new Part(userPrompt)));
        GenerationConfig config = new GenerationConfig(
                0.7,
                responseMimeType,
                new ThinkingConfig(0)
        );

        return new GeminiRequest(systemInstruction, List.of(userContent), config);
    }

    public record SystemInstruction(List<Part> parts) {}
    public record Content(String role, List<Part> parts) {}
    public record Part(String text) {}

    public record GenerationConfig(
            double temperature,
            @JsonProperty("responseMimeType") String responseMimeType,
            @JsonProperty("thinkingConfig") ThinkingConfig thinkingConfig
    ) {}

    public record ThinkingConfig(
            @JsonProperty("thinkingBudget") int thinkingBudget
    ) {}
}
