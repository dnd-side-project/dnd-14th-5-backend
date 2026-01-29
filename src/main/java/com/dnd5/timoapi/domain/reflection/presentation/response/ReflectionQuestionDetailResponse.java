package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionQuestion;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;

public record ReflectionQuestionDetailResponse(
        Long id,
        Long sequence,
        ZtpiCategory category,
        String content,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ReflectionQuestionDetailResponse from(ReflectionQuestion model) {
        return new ReflectionQuestionDetailResponse(
                model.id(),
                model.sequence(),
                model.category(),
                model.content(),
                model.createdBy(),
                model.createdAt(),
                model.updatedAt()
        );
    }
}
