package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import java.time.LocalDateTime;

public record UserTestRecordDetailResponse(
        Long id,
        Long testId,
        TestRecordStatus status,
        LocalDateTime createdAt
) {
    public static UserTestRecordDetailResponse from(UserTestRecord model) {
        return new UserTestRecordDetailResponse(
                model.id(),
                model.testId(),
                model.status(),
                model.createdAt()
        );
    }
}
