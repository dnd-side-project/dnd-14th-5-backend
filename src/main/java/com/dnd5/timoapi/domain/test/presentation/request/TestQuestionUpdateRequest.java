package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestQuestionUpdateRequest(
        @NotNull
        ZtpiCategory category,
        @NotBlank
        String content,
        int sequence,
        boolean isReversed
) {
}
