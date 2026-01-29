package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionQuestion;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;

public record ReflectionQuestionResponse(
        Long id,
        Long sequence,
        ZtpiCategory category,
        String content,
        String createdBy,
        LocalDateTime createdAt
) {

    public static ReflectionQuestionResponse from(ReflectionQuestion model) {
        return new ReflectionQuestionResponse(
                model.id(),
                model.sequence(),
                model.category(),
                model.content(),
                model.createdBy(),
                model.createdAt()
        );
    }
}
