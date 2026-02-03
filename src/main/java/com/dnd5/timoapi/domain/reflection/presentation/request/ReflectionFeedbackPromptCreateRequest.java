package com.dnd5.timoapi.domain.reflection.presentation.request;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedbackPrompt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ReflectionFeedbackPromptCreateRequest(
        @Positive
        int version,
        @NotBlank
        String content
) {
    public ReflectionFeedbackPrompt toModel() {
        return ReflectionFeedbackPrompt.create(version, content);
    }
}
