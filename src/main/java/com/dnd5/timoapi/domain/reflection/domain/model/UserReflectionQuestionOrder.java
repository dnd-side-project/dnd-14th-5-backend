package com.dnd5.timoapi.domain.reflection.domain.model;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

public record UserReflectionQuestionOrder(
        Long id,
        Long userId,
        ZtpiCategory category,
        Long sequence
) {

    public static UserReflectionQuestionOrder create(
            Long userId,
            ZtpiCategory category,
            Long sequence
    ) {
        return new UserReflectionQuestionOrder(
                null,
                userId,
                category,
                sequence
        );
    }
}
