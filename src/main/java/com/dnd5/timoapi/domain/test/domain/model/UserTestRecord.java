package com.dnd5.timoapi.domain.test.domain.model;

import java.time.LocalDateTime;

public record UserTestRecord(
        Long id,
        Long userId,
        Long testId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserTestRecord create(
            Long userId,
            Long testId,
            String status
    ) {
        return new UserTestRecord(
                null,
                userId,
                testId,
                status,
                null,
                null
        );
    }
}
