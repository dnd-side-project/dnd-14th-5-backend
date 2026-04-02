package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.user.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import java.time.LocalDateTime;

public record UserTestRecordResponse(
        Long id,
        Long testId,
        UserTestRecordStatus status,
        LocalDateTime createdAt
) {
    public static UserTestRecordResponse from(UserTestRecord model) {
        return new UserTestRecordResponse(
                model.id(),
                model.testId(),
                model.status(),
                model.createdAt()
        );
    }
}
