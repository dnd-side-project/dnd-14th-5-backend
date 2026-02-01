package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import jakarta.validation.constraints.NotNull;

public record UserTestRecordCreateRequest(
        @NotNull
        Long userId,
        @NotNull
        Long testId
) {
    public UserTestRecord toModel() {
        return UserTestRecord.create(userId, testId, "IN_PROGRESS");
    }
}
