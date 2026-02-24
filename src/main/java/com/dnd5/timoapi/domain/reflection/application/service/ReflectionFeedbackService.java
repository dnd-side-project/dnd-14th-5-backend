package com.dnd5.timoapi.domain.reflection.application.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReflectionFeedbackService {

    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final ReflectionRepository reflectionRepository;
    private final ReflectionFeedbackAsyncService reflectionFeedbackAsyncService;

    public ReflectionFeedbackDetailResponse create(Long reflectionId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));
        validateOwnership(reflectionEntity, currentUserId);

        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository.findByReflectionId(reflectionId)
                .map(existing -> {
                    if (existing.getStatus() != FeedbackStatus.FAILED) {
                        throw new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_ALREADY_EXISTS);
                    }
                    log.info("Retrying failed reflection feedback generation for reflectionId={}", reflectionId);
                    return existing;
                })
                .orElseGet(() -> reflectionFeedbackRepository.save(ReflectionFeedbackEntity.from(
                        new ReflectionFeedback(null, reflectionId, 0, null, FeedbackStatus.PENDING, null)
                )));

        reflectionFeedbackAsyncService.execute(reflectionId, feedbackEntity.getId());

        return ReflectionFeedbackDetailResponse.from(feedbackEntity.toModel());
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
}
