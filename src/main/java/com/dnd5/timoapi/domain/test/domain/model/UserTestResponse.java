package com.dnd5.timoapi.domain.test.domain.model;

import java.time.LocalDateTime;

public record UserTestResponse(
        Long id,
        Long testRecordId,
        Long questionId,
        int answerScore,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserTestResponse create(
            Long testRecordId,
            Long questionId,
            int answerScore
    ) {
        return new UserTestResponse(
                null,
                testRecordId,
                questionId,
                answerScore,
                null,
                null
        );
    }
}
