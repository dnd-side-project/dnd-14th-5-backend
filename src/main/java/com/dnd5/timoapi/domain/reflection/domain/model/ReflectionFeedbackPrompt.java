package com.dnd5.timoapi.domain.reflection.domain.model;

import java.time.LocalDateTime;

public record ReflectionFeedbackPrompt(
        Long id,
        int version,
        String content,
        LocalDateTime createdAt
) {
    public static ReflectionFeedbackPrompt create(int version, String content) {
        return new ReflectionFeedbackPrompt(null, version, content, null);
    }
}
