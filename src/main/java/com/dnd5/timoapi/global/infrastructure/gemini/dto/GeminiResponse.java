package com.dnd5.timoapi.global.infrastructure.gemini.dto;

import java.util.List;

public record GeminiResponse(
        List<Candidate> candidates
) {
    public record Candidate(Content content) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}
