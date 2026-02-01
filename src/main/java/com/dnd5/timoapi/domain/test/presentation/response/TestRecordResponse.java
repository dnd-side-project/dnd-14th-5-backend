package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.TestRecord;
import java.time.LocalDateTime;

public record TestRecordResponse(
        Long id,
        Long userId,
        Long testId,
        String status,
        LocalDateTime createdAt
) {
    public static TestRecordResponse from(TestRecord model) {
        return new TestRecordResponse(
                model.id(),
                model.userId(),
                model.testId(),
                model.status(),
                model.createdAt()
        );
    }
}
