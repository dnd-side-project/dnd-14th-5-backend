package com.dnd5.timoapi.domain.reflection.infrastructure.ai;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.infrastructure.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FeedbackGeneratorImpl implements FeedbackGenerator {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    public FeedbackResult execute(ZtpiCategory category, String userReflection) {
        String prompt = buildPrompt(category, userReflection);
        String response = geminiClient.generateContent(prompt);
        return parseResponse(response);
    }

    // FIXME: @didfodms 프롬프트 작성 / 프롬프트도 데이터베이스에 저장하면 어떨까요?
    private String buildPrompt(ZtpiCategory category, String userReflection) {
        return """
                사용자가 부족한 시간 관점 카테고리: %s
                사용자의 회고 내용: %s

                위 내용을 바탕으로 1~100점 사이의 점수와 피드백을 JSON 형식으로 제공해주세요.
                응답 형식: {"score": 점수, "content": "피드백 내용"}
                """.formatted(category.name(), userReflection);
    }

    private FeedbackResult parseResponse(String response) {
        try {
            String cleaned = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return objectMapper.readValue(cleaned, FeedbackResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }
}
