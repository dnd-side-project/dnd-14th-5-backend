package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedbackPrompt;

public record ReflectionFeedbackPromptResponse(
        Long id,
        int version,
        String content
) {

    public static ReflectionFeedbackPromptResponse from(ReflectionFeedbackPrompt model) {
        return new ReflectionFeedbackPromptResponse(
                model.id(),
                model.version(),
                model.content()
        );
    }
}
