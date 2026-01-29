package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.TestQuestion;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

import java.time.LocalDateTime;

public record TestQuestionResponse(
        Long id,
        Long testId,
        ZtpiCategory category,
        String content,
        int sequence,
        boolean isReversed,
        LocalDateTime createdAt
) {
    public static TestQuestionResponse from(TestQuestion model) {
        return new TestQuestionResponse(
                model.id(),
                model.testId(),
                model.category(),
                model.content(),
                model.sequence(),
                model.isReversed(),
                model.createdAt()
        );
    }
}
