package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.user.domain.model.UserTestRecord;

public record UserTestRecordCreateResponse(
        Long id,
        boolean isExisting
) {
    public static UserTestRecordCreateResponse from(UserTestRecord model, boolean isExisting) {
        return new UserTestRecordCreateResponse(model.id(), isExisting);
    }
}
