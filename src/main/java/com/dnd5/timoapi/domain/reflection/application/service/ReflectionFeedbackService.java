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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReflectionFeedbackService {

    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final ReflectionRepository reflectionRepository;
    private final FeedbackGenerator feedbackGenerator;
    private final ReflectionQuestionRepository reflectionQuestionRepository;

    public ReflectionFeedbackDetailResponse create(Long reflectionId) {
        if (reflectionFeedbackRepository.existsByReflectionId(reflectionId)) {
            throw new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_ALREADY_EXISTS);
        }
        ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));

        ReflectionQuestionEntity reflectionQuestionEntity =
                reflectionQuestionRepository.findById(reflectionEntity.getQuestionId())
                        .orElseThrow(() -> new BusinessException(
                                ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND));

        ReflectionFeedback processingFeedback = new ReflectionFeedback(
                null,
                reflectionEntity.getId(),
                0,
                null,
                FeedbackStatus.PROCESSING,
                null
        );
        ReflectionFeedbackEntity feedbackEntity =
                reflectionFeedbackRepository.save(ReflectionFeedbackEntity.from(processingFeedback));

        try {
            FeedbackResult feedbackResult = feedbackGenerator.execute(
                    reflectionQuestionEntity.getCategory(),
                    reflectionQuestionEntity.getContent(),
                    reflectionEntity.getAnswerText()
            );
            feedbackEntity.complete(feedbackResult.score(), feedbackResult.content());
        } catch (Exception e) {
            log.error("Feedback generation failed for reflectionId={}", reflectionId, e);
            feedbackEntity.fail();
        }

        return ReflectionFeedbackDetailResponse.from(feedbackEntity.toModel());
    }
}
