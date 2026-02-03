package com.dnd5.timoapi.domain.reflection.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record ReflectionFeedbackPromptUpdateRequest(
        @NotBlank
        String content
) {
}
