package com.dnd5.timoapi.domain.reflection.domain.model;

import java.time.LocalDateTime;

public record ReflectionFeedbackPrompt(
        Long id,
        int version,
        String content,
        LocalDateTime createdAt
) {

}
