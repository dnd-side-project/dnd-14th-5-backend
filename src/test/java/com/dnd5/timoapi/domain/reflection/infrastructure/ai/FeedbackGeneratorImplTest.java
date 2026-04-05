package com.dnd5.timoapi.domain.reflection.infrastructure.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackPromptEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackPromptRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.infrastructure.gemini.GeminiClient;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class FeedbackGeneratorImplTest {

    @InjectMocks
    private FeedbackGeneratorImpl feedbackGeneratorImpl;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private ReflectionFeedbackPromptRepository reflectionFeedbackPromptRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String MOCK_PROMPT = "카테고리: %s, 질문: %s, 회고: %s";

    @BeforeEach
    void setUp() {
        ReflectionFeedbackPromptEntity promptEntity = new ReflectionFeedbackPromptEntity(1, MOCK_PROMPT);
        when(reflectionFeedbackPromptRepository.findTopByDeletedAtIsNullOrderByVersionDesc())
                .thenReturn(Optional.of(promptEntity));
    }

    @Test
    void execute_정상_JSON_응답이면_파싱_성공() {
        when(geminiClient.generateContent(any(), any())).thenReturn("{\"score\": 80, \"content\": \"피드백 내용\"}");

        FeedbackResult result = feedbackGeneratorImpl.execute(ZtpiCategory.FUTURE, "질문", "회고 내용");

        assertThat(result.score()).isEqualTo(80);
        assertThat(result.content()).isEqualTo("피드백 내용");
    }

    @Test
    void execute_마크다운으로_감싼_JSON_응답이면_파싱_성공() {
        when(geminiClient.generateContent(any(), any()))
                .thenReturn("```json\n{\"score\": 75, \"content\": \"마크다운 피드백\"}\n```");

        FeedbackResult result = feedbackGeneratorImpl.execute(ZtpiCategory.FUTURE, "질문", "회고 내용");

        assertThat(result.score()).isEqualTo(75);
        assertThat(result.content()).isEqualTo("마크다운 피드백");
    }

    @Test
    void execute_앞뒤_텍스트가_있는_JSON_응답이면_JSON_추출_후_파싱_성공() {
        when(geminiClient.generateContent(any(), any()))
                .thenReturn("다음은 피드백 결과입니다.\n{\"score\": 60, \"content\": \"본문 피드백\"}\n감사합니다.");

        FeedbackResult result = feedbackGeneratorImpl.execute(ZtpiCategory.FUTURE, "질문", "회고 내용");

        assertThat(result.score()).isEqualTo(60);
        assertThat(result.content()).isEqualTo("본문 피드백");
    }

    @Test
    void execute_빈_응답이면_기본값_반환() {
        when(geminiClient.generateContent(any(), any())).thenReturn("");

        FeedbackResult result = feedbackGeneratorImpl.execute(ZtpiCategory.FUTURE, "질문", "회고 내용");

        assertThat(result.score()).isEqualTo(0);
        assertThat(result.content()).isEqualTo("");
    }

    @Test
    void execute_JSON_형식이_아닌_응답이면_예외() {
        when(geminiClient.generateContent(any(), any())).thenReturn("이것은 JSON이 아닙니다.");

        assertThatThrownBy(() -> feedbackGeneratorImpl.execute(ZtpiCategory.FUTURE, "질문", "회고 내용"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse Gemini response");
    }
}
