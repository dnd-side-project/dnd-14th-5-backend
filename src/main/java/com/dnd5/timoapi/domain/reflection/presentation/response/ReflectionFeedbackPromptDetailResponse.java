package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedbackPrompt;
import java.time.LocalDateTime;

public record ReflectionFeedbackPromptDetailResponse(
        Long id,
        int version,
        String content,
        LocalDateTime createdAt
) {

    public static ReflectionFeedbackPromptDetailResponse from(ReflectionFeedbackPrompt model) {
        return new ReflectionFeedbackPromptDetailResponse(
                model.id(),
                model.version(),
                model.content(),
                model.createdAt()
        );
    }
}