package com.dnd5.timoapi.domain.test.domain.model;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

import java.time.LocalDateTime;

public record TestQuestion(
        Long id,
        Long testId,
        ZtpiCategory category,
        String content,
        int sequence,
        boolean isReversed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TestQuestion create(
            Long testId,
            ZtpiCategory category,
            String content,
            int sequence,
            boolean isReversed
    ) {
        return new TestQuestion(
                null,
                testId,
                category,
                content,
                sequence,
                isReversed,
                null,
                null
        );
    }
}
