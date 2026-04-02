package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.test.presentation.response.TestResultResponse;
import com.dnd5.timoapi.domain.user.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import java.time.LocalDateTime;

public record UserTestRecordDetailResponse(
        Long id,
        Long testId,
        UserTestRecordStatus status,
        Integer progress,
        TestResultResponse result,
        LocalDateTime createdAt
) {
    public static UserTestRecordDetailResponse of(
            UserTestRecord model,
            Integer progress,
            TestResultResponse result
    ) {
        return new UserTestRecordDetailResponse(
                model.id(),
                model.testId(),
                model.status(),
                progress,
                result,
                model.createdAt()
        );
    }
}
