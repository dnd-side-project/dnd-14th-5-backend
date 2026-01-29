package com.dnd5.timoapi.domain.reflection.domain.model;

import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import java.time.LocalDateTime;

public record ReflectionFeedback(
        Long id,
        Long reflectionId,
        int score,
        String content,
        FeedbackStatus status,
        LocalDateTime createdAt
) {

}
