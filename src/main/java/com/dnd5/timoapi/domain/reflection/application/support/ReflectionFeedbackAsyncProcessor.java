package com.dnd5.timoapi.domain.reflection.application.support;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackGenerator;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackResult;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReflectionFeedbackAsyncProcessor {

    private static final int MIN_FEEDBACK_SCORE = 0;
    private static final int MAX_FEEDBACK_SCORE = 5;

    private final ReflectionRepository reflectionRepository;
    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;
    private final FeedbackGenerator feedbackGenerator;

    @Async("feedbackTaskExecutor")
    @Transactional
    public void process(Long reflectionId) {
        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository
                .findByReflectionId(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_NOT_FOUND));

        try {
            ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                    .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));

            ReflectionQuestionEntity questionEntity = reflectionQuestionRepository
                    .findById(reflectionEntity.getQuestionId())
                    .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND));

            FeedbackResult feedbackResult = feedbackGenerator.execute(
                    questionEntity.getCategory(),
                    questionEntity.getContent(),
                    reflectionEntity.getAnswerText()
            );
            validateScore(feedbackResult.score());
            feedbackEntity.complete(feedbackResult.score(), feedbackResult.content());
        } catch (Exception e) {
            log.error("Feedback generation failed for reflectionId={}", reflectionId, e);
            feedbackEntity.fail();
        }
    }

    @Transactional
    public void markAsFailed(Long reflectionId) {
        reflectionFeedbackRepository.findByReflectionId(reflectionId)
                .ifPresent(ReflectionFeedbackEntity::fail);
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
}
