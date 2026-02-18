package com.dnd5.timoapi.domain.test.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record UserTestResponseUpdateRequest(
        @Positive
        @Min(value = 1)
        @Max(value = 5)
        int score
) {
}
