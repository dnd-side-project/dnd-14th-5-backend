package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.group.domain.model.Group;

public record GroupCreateResponse(
        Long id,
        String code
) {
    public static GroupCreateResponse from(Group model) {
        return new GroupCreateResponse(model.id(), model.code());
    }
}
