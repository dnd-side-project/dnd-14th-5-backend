package com.dnd5.timoapi.domain.reflection.infrastructure.ai;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackPromptEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackPromptRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.infrastructure.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FeedbackGeneratorImpl implements FeedbackGenerator {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private final ReflectionFeedbackPromptRepository reflectionFeedbackPromptRepository;

    @Override
    public FeedbackResult execute(ZtpiCategory category, String question, String userReflection) {
        String prompt = buildPrompt(category, question, userReflection);
        String response = geminiClient.generateContent(prompt);
        if (response == null || response.isBlank()) {
            return new FeedbackResult(0, "");
        }
        return parseResponse(response);
    }

    // FIXME: @didfodms 프롬프트 작성 / 프롬프트도 데이터베이스에 저장하면 어떨까요?
    private String buildPrompt(ZtpiCategory category, String question, String userReflection) {
        return getLatestPrompt().formatted(category.name(), question, userReflection);
    }

    private FeedbackResult parseResponse(String response) {
        try {
            String cleaned = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return objectMapper.readValue(cleaned, FeedbackResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    private String getLatestPrompt() {
        ReflectionFeedbackPromptEntity reflectionFeedbackPromptEntity =  reflectionFeedbackPromptRepository.findTopByDeletedAtIsNullOrderByVersionDesc()
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_PROMPT_NOT_FOUND));

        return reflectionFeedbackPromptEntity.getContent();
    }

}
