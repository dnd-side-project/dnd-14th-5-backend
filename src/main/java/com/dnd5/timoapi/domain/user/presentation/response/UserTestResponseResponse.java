package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.user.domain.model.UserTestResponse;

public record UserTestResponseResponse(
        Long id,
        Long testRecordId,
        Long questionId,
        int score
) {
    public static UserTestResponseResponse from(UserTestResponse model) {
        return new UserTestResponseResponse(
                model.id(),
                model.testRecordId(),
                model.questionId(),
                model.answerScore()
        );
    }
}
