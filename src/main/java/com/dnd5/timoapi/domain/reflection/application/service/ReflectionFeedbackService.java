package com.dnd5.timoapi.domain.reflection.application.service;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackGenerator;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackResult;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackDetailResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReflectionFeedbackService {

    private static final int MIN_FEEDBACK_SCORE = 0;
    private static final int MAX_FEEDBACK_SCORE = 100;

    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final ReflectionRepository reflectionRepository;
    private final FeedbackGenerator feedbackGenerator;
    private final ReflectionQuestionRepository reflectionQuestionRepository;

    public ReflectionFeedbackDetailResponse create(Long reflectionId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String failureReason = null;
        ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));
        validateOwnership(reflectionEntity, currentUserId);

        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository.findByReflectionId(reflectionId)
                .map(existingFeedback -> prepareFeedbackForRegeneration(reflectionId, existingFeedback))
                .orElseGet(() -> reflectionFeedbackRepository.save(ReflectionFeedbackEntity.from(
                        new ReflectionFeedback(
                                null,
                                reflectionEntity.getId(),
                                0,
                                null,
                                FeedbackStatus.PROCESSING,
                                null
                        )
                )));

        ReflectionQuestionEntity reflectionQuestionEntity =
                reflectionQuestionRepository.findById(reflectionEntity.getQuestionId())
                        .orElseThrow(() -> new BusinessException(
                                ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND));

        try {
            FeedbackResult feedbackResult = feedbackGenerator.execute(
                    reflectionQuestionEntity.getCategory(),
                    reflectionQuestionEntity.getContent(),
                    reflectionEntity.getAnswerText()
            );
            validateScore(feedbackResult.score());
            feedbackEntity.complete(feedbackResult.score(), feedbackResult.content());
        } catch (BusinessException e) {
            log.error("Feedback generation failed with business error for reflectionId={}", reflectionId, e);
            feedbackEntity.fail();
            throw e;
        } catch (Exception e) {
            log.error("Feedback generation failed for reflectionId={}", reflectionId, e);
            feedbackEntity.fail();
            failureReason = extractFailureReason(e);
        }

        return ReflectionFeedbackDetailResponse.from(feedbackEntity.toModel(), failureReason);
    }

    private void validateOwnership(ReflectionEntity reflectionEntity, Long currentUserId) {
        if (!reflectionEntity.getUserId().equals(currentUserId)) {
            throw new BusinessException(
                    ReflectionErrorCode.REFLECTION_NOT_OWNER,
                    reflectionEntity.getId(),
                    reflectionEntity.getUserId(),
                    currentUserId
            );
        }
    }

    private ReflectionFeedbackEntity prepareFeedbackForRegeneration(
            Long reflectionId,
            ReflectionFeedbackEntity existingFeedback
    ) {
        if (existingFeedback.getStatus() != FeedbackStatus.FAILED) {
            throw new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_ALREADY_EXISTS);
        }

        log.info("Retrying failed reflection feedback generation for reflectionId={}", reflectionId);
        existingFeedback.restartProcessing();
        return existingFeedback;
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
