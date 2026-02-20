package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import java.time.LocalDateTime;

public record ReflectionFeedbackDetailResponse(
        Long id,
        Long reflectionId,
        int score,
        String content,
        FeedbackStatus status,
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
                model.score(),
                model.content(),
                model.status(),
                model.createdAt(),
                failureReason
        );
    }
}
