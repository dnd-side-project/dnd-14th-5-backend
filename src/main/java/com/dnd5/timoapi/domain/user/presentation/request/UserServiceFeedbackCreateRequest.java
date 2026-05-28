package com.dnd5.timoapi.domain.user.presentation.request;

import com.dnd5.timoapi.domain.user.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserTestRecordCreateRequest(
        @Positive @NotNull
        Long testId
) {
    public UserTestRecord toModel() {
        return UserTestRecord.create(testId, UserTestRecordStatus.IN_PROGRESS);
    }
}
