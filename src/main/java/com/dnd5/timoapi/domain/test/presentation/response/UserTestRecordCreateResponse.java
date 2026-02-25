package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;

public record UserTestRecordCreateResponse(
        Long id,
        boolean isExisting
) {
    public static UserTestRecordCreateResponse from(UserTestRecord model, boolean isExisting) {
        return new UserTestRecordCreateResponse(model.id(), isExisting);
    }
}
