package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

import java.time.LocalDateTime;

public record ReflectionFeedbackDetailResponse(
        Long id,
        Long reflectionId,
        ZtpiCategory category,
        int score,
        String content,
        FeedbackStatus status,
        Boolean isIncreased,
        Double changedScore,
        Double beforeScore,
        Double afterScore,
        LocalDateTime createdAt,
        String failureReason
) {

    public static ReflectionFeedbackDetailResponse from(ReflectionFeedback model) {
        return from(model, null);
    }

    public static ReflectionFeedbackDetailResponse from(ReflectionFeedback model, String failureReason) {
        return new ReflectionFeedbackDetailResponse(
                model.id(),
                model.reflectionId(),
                model.category(),
                model.score(),
                model.content(),
                model.status(),
                model.isIncreased(),
                model.changedScore(),
                model.beforeScore(),
                model.afterScore(),
                model.createdAt(),
                failureReason
        );
    }
}
