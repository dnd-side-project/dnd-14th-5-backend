package com.dnd5.timoapi.global.infrastructure.gemini;

import com.dnd5.timoapi.global.infrastructure.gemini.dto.GeminiRequest;
import com.dnd5.timoapi.global.infrastructure.gemini.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient geminiWebClient;
    private final GeminiProperties properties;

    public String generateContent(String systemPrompt, String userPrompt) {
        log.info("gemini_request_start systemPromptLen={} userPromptLen={}", systemPrompt.length(), userPrompt.length());
        long startMs = System.currentTimeMillis();

        GeminiRequest request = GeminiRequest.of(systemPrompt, userPrompt);

        List<GeminiResponse> responses;
        try {
            responses = geminiWebClient.post()
                    .uri("/publishers/google/models/{model}:streamGenerateContent?key={apiKey}",
                            properties.model(), properties.apiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(GeminiResponse.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("gemini_request_failed reason=exception latencyMs={}", System.currentTimeMillis() - startMs, e);
            throw e;
        }

        if (responses == null || responses.isEmpty()) {
            log.error("gemini_request_failed reason=empty_response latencyMs={}", System.currentTimeMillis() - startMs);
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

        log.info("gemini_request_done latencyMs={} responseLen={}", System.currentTimeMillis() - startMs, result.length());
        return result.toString();
    }
}
