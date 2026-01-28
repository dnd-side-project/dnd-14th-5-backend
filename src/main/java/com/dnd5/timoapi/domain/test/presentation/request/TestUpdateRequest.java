package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;

public record TestUpdateRequest(
        TestType type,
        String name,
        String description
) {
}
