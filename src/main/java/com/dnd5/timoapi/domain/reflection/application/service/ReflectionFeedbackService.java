package com.dnd5.timoapi.domain.reflection.application.service;

import com.dnd5.timoapi.domain.reflection.application.support.ReflectionFeedbackAsyncProcessor;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackDetailResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReflectionFeedbackService {

    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final ReflectionRepository reflectionRepository;
    private final ReflectionFeedbackAsyncProcessor reflectionFeedbackAsyncProcessor;

    public ReflectionFeedbackDetailResponse create(Long reflectionId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));
        validateOwnership(reflectionEntity, currentUserId);

        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository
                .findByReflectionId(reflectionId)
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

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    reflectionFeedbackAsyncProcessor.process(reflectionId);
                } catch (Exception e) {
                    log.error("Failed to submit async feedback task for reflectionId={}", reflectionId, e);
                    reflectionFeedbackAsyncProcessor.markAsFailed(reflectionId);
                }
            }
        });

        return ReflectionFeedbackDetailResponse.from(feedbackEntity.toModel());
    }

    @Transactional(readOnly = true)
    public ReflectionFeedbackDetailResponse findByReflectionId(Long reflectionId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));
        validateOwnership(reflectionEntity, currentUserId);

        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository
                .findByReflectionId(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_NOT_FOUND));

        return ReflectionFeedbackDetailResponse.from(feedbackEntity.toModel());
    }

    public void delete(Long feedbackId) {
        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_NOT_FOUND));
        reflectionFeedbackRepository.delete(feedbackEntity);
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
}
