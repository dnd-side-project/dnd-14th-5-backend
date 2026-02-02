package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.UserTestResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserTestResponseCreateRequest(
        @Positive @NotNull
        Long questionId,
        @Positive
        int score
) {
    public UserTestResponse toModel(@Positive @NotNull Long testRecordId) {
        return UserTestResponse.create(testRecordId, questionId, score);
    }
}
