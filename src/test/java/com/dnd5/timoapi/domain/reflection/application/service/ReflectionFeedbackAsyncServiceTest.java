package com.dnd5.timoapi.domain.reflection.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackGenerator;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackResult;
import com.dnd5.timoapi.domain.reflection.infrastructure.sse.FeedbackSseEmitterRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReflectionFeedbackAsyncServiceTest {

    @InjectMocks
    private ReflectionFeedbackAsyncService reflectionFeedbackAsyncService;

    @Mock
    private ReflectionFeedbackRepository reflectionFeedbackRepository;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private ReflectionQuestionRepository reflectionQuestionRepository;

    @Mock
    private FeedbackGenerator feedbackGenerator;

    @Mock
    private FeedbackSseEmitterRepository sseEmitterRepository;

    @Test
    void execute_성공시_COMPLETED_처리() {
        Long reflectionId = 10L;
        Long feedbackId = 1L;
        Long questionId = 3L;
        String answerText = "회고 내용";

        ReflectionFeedbackEntity feedbackEntity = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.PENDING)
        );
        when(reflectionFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedbackEntity));

        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, questionId, LocalDate.now(), answerText);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionQuestionEntity questionEntity = new ReflectionQuestionEntity(
                1L, ZtpiCategory.FUTURE, "질문", "system"
        );
        when(reflectionQuestionRepository.findById(questionId)).thenReturn(Optional.of(questionEntity));
        when(feedbackGenerator.execute(ZtpiCategory.FUTURE, "질문", answerText))
                .thenReturn(new FeedbackResult(80, "피드백"));
        when(sseEmitterRepository.get(reflectionId)).thenReturn(Optional.empty());

        reflectionFeedbackAsyncService.execute(reflectionId, feedbackId);

        verify(feedbackEntity).restartProcessing();
        verify(feedbackEntity).complete(80, "피드백");
        verify(feedbackEntity, never()).fail();
    }

    @Test
    void execute_score_범위_초과시_FAILED_처리() {
        Long reflectionId = 10L;
        Long feedbackId = 1L;
        Long questionId = 3L;
        String answerText = "회고 내용";

        ReflectionFeedbackEntity feedbackEntity = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.PENDING)
        );
        when(reflectionFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedbackEntity));

        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, questionId, LocalDate.now(), answerText);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionQuestionEntity questionEntity = new ReflectionQuestionEntity(
                1L, ZtpiCategory.FUTURE, "질문", "system"
        );
        when(reflectionQuestionRepository.findById(questionId)).thenReturn(Optional.of(questionEntity));
        when(feedbackGenerator.execute(ZtpiCategory.FUTURE, "질문", answerText))
                .thenReturn(new FeedbackResult(200, "피드백"));
        when(sseEmitterRepository.get(reflectionId)).thenReturn(Optional.empty());

        reflectionFeedbackAsyncService.execute(reflectionId, feedbackId);

        verify(feedbackEntity).fail();
        verify(feedbackEntity, never()).complete(anyInt(), any(String.class));
    }

    @Test
    void execute_예외_발생시_FAILED_처리() {
        Long reflectionId = 10L;
        Long feedbackId = 1L;
        Long questionId = 3L;
        String answerText = "회고 내용";

        ReflectionFeedbackEntity feedbackEntity = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.PENDING)
        );
        when(reflectionFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedbackEntity));

        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, questionId, LocalDate.now(), answerText);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionQuestionEntity questionEntity = new ReflectionQuestionEntity(
                1L, ZtpiCategory.FUTURE, "질문", "system"
        );
        when(reflectionQuestionRepository.findById(questionId)).thenReturn(Optional.of(questionEntity));
        when(feedbackGenerator.execute(ZtpiCategory.FUTURE, "질문", answerText))
                .thenThrow(new RuntimeException("Gemini timeout"));
        when(sseEmitterRepository.get(reflectionId)).thenReturn(Optional.empty());

        reflectionFeedbackAsyncService.execute(reflectionId, feedbackId);

        verify(feedbackEntity).fail();
        verify(feedbackEntity, never()).complete(anyInt(), any(String.class));
    }
}
