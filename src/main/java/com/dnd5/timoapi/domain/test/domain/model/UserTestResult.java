package com.dnd5.timoapi.domain.test.domain.model;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;

public record UserTestResult(
        Long id,
        Long testRecordId,
        ZtpiCategory category,
        float score,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserTestResult create(
            Long testRecordId,
            ZtpiCategory category,
            float score
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
