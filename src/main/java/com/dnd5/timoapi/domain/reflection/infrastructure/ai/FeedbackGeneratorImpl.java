package com.dnd5.timoapi.domain.reflection.infrastructure.ai;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackPromptEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackPromptRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.infrastructure.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackGeneratorImpl implements FeedbackGenerator {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final ReflectionFeedbackPromptRepository reflectionFeedbackPromptRepository;

    @Override
    public FeedbackResult execute(ZtpiCategory category, String question, String userReflection) {
        log.info("feedback_generate_start category={} questionLen={} reflectionLen={}", category, question.length(), userReflection.length());
        String systemPrompt = getLatestSystemPrompt();
        String userPrompt = buildUserPrompt(category, question, userReflection);
        String response = geminiClient.generateContent(systemPrompt, userPrompt);
        if (response == null || response.isBlank()) {
            return new FeedbackResult(0, "");
        }
        FeedbackResult result = parseResponse(response);
        log.info("feedback_generate_done category={} score={}", category, result.score());
        return result;
    }

    private String getLatestSystemPrompt() {
        ReflectionFeedbackPromptEntity entity = reflectionFeedbackPromptRepository
                .findTopByDeletedAtIsNullOrderByVersionDesc()
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_PROMPT_NOT_FOUND));
        return entity.getContent();
    }

    private String buildUserPrompt(ZtpiCategory category, String question, String userReflection) {
        try {
            return objectMapper.writeValueAsString(java.util.Map.of(
                    "category", category.name(),
                    "question", question,
                    "response", userReflection
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build user prompt: ", e);
        }
    }

    private FeedbackResult parseResponse(String response) {
        try {
            String cleaned = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start != -1 && end != -1 && start < end) {
                cleaned = cleaned.substring(start, end + 1);
            }
            return objectMapper.readValue(cleaned, FeedbackResult.class);
        } catch (Exception e) {
            log.error("feedback_parse_failed raw={}", response, e);
            throw new RuntimeException("Failed to parse Gemini response: ", e);
        }
    }

}
