package com.dnd5.timoapi.domain.test.domain.model;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import java.time.LocalDateTime;

public record UserTestRecord(
        Long id,
        Long userId,
        Long testId,
        TestRecordStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserTestRecord create(
            Long testId,
            TestRecordStatus status
    ) {
        return new UserTestRecord(
                null,
                getCurrentUserId(),
                testId,
                status,
                null,
                null
        );
    }
}
