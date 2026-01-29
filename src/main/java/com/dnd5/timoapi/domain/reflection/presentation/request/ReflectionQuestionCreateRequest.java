package com.dnd5.timoapi.domain.reflection.presentation.request;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionQuestion;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReflectionQuestionCreateRequest(
        @NotNull
        ZtpiCategory category,
        @NotBlank
        String content,
        @NotBlank
        String createdBy
) {
}
