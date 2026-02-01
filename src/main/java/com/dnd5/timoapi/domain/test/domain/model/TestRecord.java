package com.dnd5.timoapi.domain.test.domain.model;

import java.time.LocalDateTime;

public record TestRecord(
        Long id,
        Long userId,
        Long testId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TestRecord create(
            Long userId,
            Long testId,
            String status
    ) {
        return new TestRecord(
                null,
                userId,
                testId,
                status,
                null,
                null
        );
    }
}
