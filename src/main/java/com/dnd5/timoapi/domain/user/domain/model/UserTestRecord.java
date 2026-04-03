package com.dnd5.timoapi.domain.user.domain.model;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import java.time.LocalDateTime;

public record UserTestRecord(
        Long id,
        Long userId,
        Long testId,
        UserTestRecordStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserTestRecord create(
            Long testId,
            UserTestRecordStatus status
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
