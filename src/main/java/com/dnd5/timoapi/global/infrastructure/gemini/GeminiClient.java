package com.dnd5.timoapi.global.infrastructure.gemini;

import com.dnd5.timoapi.global.infrastructure.gemini.dto.GeminiRequest;
import com.dnd5.timoapi.global.infrastructure.gemini.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient geminiWebClient;
    private final GeminiProperties properties;

    public String generateContent(String prompt) {
        GeminiRequest request = GeminiRequest.of(prompt);

        List<GeminiResponse> responses = geminiWebClient.post()
                .uri("/publishers/google/models/{model}:streamGenerateContent?key={apiKey}",
                        properties.model(), properties.apiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(GeminiResponse.class)
                .collectList()
                .block();

        if (responses == null || responses.isEmpty()) {
            throw new RuntimeException("Gemini API response is empty");
        }

        StringBuilder result = new StringBuilder();
        for (GeminiResponse response : responses) {
            if (response.candidates() != null && !response.candidates().isEmpty()) {
                var parts = response.candidates().get(0).content().parts();
                if (parts != null && !parts.isEmpty()) {
                    result.append(parts.get(0).text());
                }
            }
        }

        return result.toString();
    }
}
