package com.dnd5.timoapi.domain.user.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserTestResponseCreateRequest(
        @Positive @NotNull
        Long questionId,
        @Positive
        int score
) {
}
