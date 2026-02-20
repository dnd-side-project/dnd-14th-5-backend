package com.dnd5.timoapi.domain.reflection.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackDetailResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReflectionFeedbackServiceTest {

    @InjectMocks
    private ReflectionFeedbackService reflectionFeedbackService;

    @Mock
    private ReflectionFeedbackRepository reflectionFeedbackRepository;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private FeedbackGenerator feedbackGenerator;

    @Mock
    private ReflectionQuestionRepository reflectionQuestionRepository;

    @Test
    void create_다른_유저_회고면_403_예외() {
        Long reflectionId = 10L;
        ReflectionEntity reflectionEntity = new ReflectionEntity(2L, 3L, LocalDate.now(), "text");
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> reflectionFeedbackService.create(reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException businessException = (BusinessException) exception;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ReflectionErrorCode.REFLECTION_NOT_OWNER);
                    });
        }

        verify(reflectionFeedbackRepository, never()).findByReflectionId(any());
    }

    @Test
    void create_FAILED_기존_피드백은_재생성_허용() {
        Long reflectionId = 10L;
        Long questionId = 3L;
        String answerText = "회고 내용";

        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, questionId, LocalDate.now(), answerText);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionFeedbackEntity failedFeedback = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.FAILED)
        );
        when(reflectionFeedbackRepository.findByReflectionId(reflectionId))
                .thenReturn(Optional.of(failedFeedback));

        ReflectionQuestionEntity questionEntity = new ReflectionQuestionEntity(
                1L, ZtpiCategory.FUTURE, "질문", "system"
        );
        when(reflectionQuestionRepository.findById(questionId)).thenReturn(Optional.of(questionEntity));
        when(feedbackGenerator.execute(ZtpiCategory.FUTURE, "질문", answerText))
                .thenReturn(new FeedbackResult(80, "피드백"));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            ReflectionFeedbackDetailResponse response = reflectionFeedbackService.create(reflectionId);

            assertThat(response.score()).isEqualTo(80);
            assertThat(response.status()).isEqualTo(FeedbackStatus.COMPLETED);
            assertThat(response.failureReason()).isNull();
        }

        verify(failedFeedback).restartProcessing();
        verify(failedFeedback).complete(80, "피드백");
        verify(reflectionFeedbackRepository, never()).save(any());
    }

    @Test
    void create_score_범위_초과시_예외_반환() {
        Long reflectionId = 10L;
        Long questionId = 3L;
        String answerText = "회고 내용";

        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, questionId, LocalDate.now(), answerText);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionFeedbackEntity processingFeedback = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.PROCESSING)
        );
        when(reflectionFeedbackRepository.findByReflectionId(reflectionId)).thenReturn(Optional.empty());
        when(reflectionFeedbackRepository.save(any(ReflectionFeedbackEntity.class))).thenReturn(processingFeedback);

        ReflectionQuestionEntity questionEntity = new ReflectionQuestionEntity(
                1L, ZtpiCategory.FUTURE, "질문", "system"
        );
        when(reflectionQuestionRepository.findById(questionId)).thenReturn(Optional.of(questionEntity));
        when(feedbackGenerator.execute(ZtpiCategory.FUTURE, "질문", answerText))
                .thenReturn(new FeedbackResult(200, "피드백"));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> reflectionFeedbackService.create(reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException businessException = (BusinessException) exception;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ReflectionErrorCode.REFLECTION_FEEDBACK_SCORE_OUT_OF_RANGE);
                    });
        }

        verify(processingFeedback).fail();
        verify(processingFeedback, never()).complete(anyInt(), any(String.class));
    }

    @Test
    void create_예외로_FAILED_처리되면_실패원인_반환() {
        Long reflectionId = 10L;
        Long questionId = 3L;
        String answerText = "회고 내용";

        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, questionId, LocalDate.now(), answerText);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionFeedbackEntity processingFeedback = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.PROCESSING)
        );
        when(reflectionFeedbackRepository.findByReflectionId(reflectionId)).thenReturn(Optional.empty());
        when(reflectionFeedbackRepository.save(any(ReflectionFeedbackEntity.class))).thenReturn(processingFeedback);

        ReflectionQuestionEntity questionEntity = new ReflectionQuestionEntity(
                1L, ZtpiCategory.FUTURE, "질문", "system"
        );
        when(reflectionQuestionRepository.findById(questionId)).thenReturn(Optional.of(questionEntity));
        when(feedbackGenerator.execute(ZtpiCategory.FUTURE, "질문", answerText))
                .thenThrow(new RuntimeException("Gemini timeout"));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            ReflectionFeedbackDetailResponse response = reflectionFeedbackService.create(reflectionId);
            assertThat(response.status()).isEqualTo(FeedbackStatus.FAILED);
            assertThat(response.failureReason()).isEqualTo("Gemini timeout");
        }

        verify(processingFeedback).fail();
    }
}
