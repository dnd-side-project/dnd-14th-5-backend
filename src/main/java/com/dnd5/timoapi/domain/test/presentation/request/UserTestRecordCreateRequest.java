package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserTestRecordCreateRequest(
        @Positive @NotNull
        Long testId
) {
    public UserTestRecord toModel() {
        return UserTestRecord.create(testId, TestRecordStatus.IN_PROGRESS);
    }
}
