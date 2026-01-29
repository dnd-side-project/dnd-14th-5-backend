package com.dnd5.timoapi.domain.reflection.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

public record ReflectionQuestionUpdateRequest(
        String content,
        String createdBy
) {
}
