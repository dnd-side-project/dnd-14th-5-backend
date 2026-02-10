package com.dnd5.timoapi.domain.test.domain.model;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;

public record UserTestResult(
        Long id,
        Long testRecordId,
        ZtpiCategory category,
        double score,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserTestResult create(
            Long testRecordId,
            ZtpiCategory category,
            double score
    ) {
        return new UserTestResult(
                null,
                testRecordId,
                category,
                score,
                null,
                null
        );
    }
}
