package com.dnd5.timoapi.domain.reflection.domain.model;

import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;

public record ReflectionFeedback(
        Long id,
        Long reflectionId,
        int score,
        String content,
        FeedbackStatus status,
        ZtpiCategory category,
        Boolean isIncreased,
        Double changedScore,
        Double beforeScore,
        Double afterScore,
        LocalDateTime createdAt
) {

}
