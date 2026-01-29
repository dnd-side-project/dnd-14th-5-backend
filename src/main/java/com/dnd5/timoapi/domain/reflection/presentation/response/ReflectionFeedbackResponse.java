package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import java.time.LocalDateTime;

public record ReflectionFeedbackResponse(
        Long id,
        Long reflectionId,
        int score,
        String content,
        FeedbackStatus status,
        LocalDateTime createdAt
) {

    public static ReflectionFeedbackResponse from(ReflectionFeedback model) {
        return new ReflectionFeedbackResponse(
                model.id(),
                model.reflectionId(),
                model.score(),
                model.content(),
                model.status(),
                model.createdAt()
        );
    }
}
