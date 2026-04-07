package com.dnd5.timoapi.domain.reflection.application.service;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackGenerator;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackResult;
import com.dnd5.timoapi.domain.reflection.infrastructure.sse.FeedbackSseEmitterRepository;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackDetailResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReflectionFeedbackAsyncService {

    private static final int MIN_FEEDBACK_SCORE = 0;
    private static final int MAX_FEEDBACK_SCORE = 100;

    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final ReflectionRepository reflectionRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;
    private final FeedbackGenerator feedbackGenerator;
    private final FeedbackSseEmitterRepository sseEmitterRepository;

    @Async
    @Transactional
    public void execute(Long reflectionId, Long feedbackId) {
        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_NOT_FOUND));

        feedbackEntity.restartProcessing();

        ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));

        ReflectionQuestionEntity questionEntity = reflectionQuestionRepository
                .findById(reflectionEntity.getQuestionId())
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND));

        String failureReason = null;
        try {
            FeedbackResult result = feedbackGenerator.execute(
                    questionEntity.getCategory(),
                    questionEntity.getContent(),
                    reflectionEntity.getAnswerText()
            );
            validateScore(result.score());
            feedbackEntity.complete(result.score(), result.content());
        } catch (BusinessException e) {
            log.error("Feedback generation failed with business error for reflectionId={}", reflectionId, e);
            feedbackEntity.fail();
        } catch (Exception e) {
            log.error("Feedback generation failed for reflectionId={}", reflectionId, e);
            feedbackEntity.fail();
            failureReason = extractFailureReason(e);
        }

        sendSseEvent(reflectionId, feedbackEntity, failureReason);
    }

    private void sendSseEvent(Long reflectionId, ReflectionFeedbackEntity feedbackEntity, String failureReason) {
        sseEmitterRepository.get(reflectionId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("feedback")
                        .data(ReflectionFeedbackDetailResponse.from(feedbackEntity.toModel(), failureReason)));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send SSE event for reflectionId={}", reflectionId, e);
                emitter.completeWithError(e);
            } finally {
                sseEmitterRepository.remove(reflectionId);
            }
        });
    }

    private void validateScore(int score) {
        if (score < MIN_FEEDBACK_SCORE || score > MAX_FEEDBACK_SCORE) {
            throw new BusinessException(
                    ReflectionErrorCode.REFLECTION_FEEDBACK_SCORE_OUT_OF_RANGE,
                    score,
                    MIN_FEEDBACK_SCORE,
                    MAX_FEEDBACK_SCORE
            );
        }
    }

    private String extractFailureReason(Exception e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        if (root.getMessage() != null && !root.getMessage().isBlank()) {
            return root.getMessage();
        }
        return "피드백 생성 중 알 수 없는 오류가 발생했습니다.";
    }
}
