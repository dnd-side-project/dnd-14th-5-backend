package com.dnd5.timoapi.domain.reflection.domain.model;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;

public record ReflectionQuestion(
        Long id,
        Long sequence,
        ZtpiCategory category,
        String content,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ReflectionQuestion create(
            Long sequence,
            ZtpiCategory category,
            String content,
            String createdBy
    ) {
        return new ReflectionQuestion(
                null,
                sequence,
                category,
                content,
                createdBy,
                null,
                null
        );
    }
}
