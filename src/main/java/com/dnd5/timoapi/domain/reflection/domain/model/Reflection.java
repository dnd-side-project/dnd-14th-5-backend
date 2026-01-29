package com.dnd5.timoapi.domain.reflection.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Reflection(
        Long id,
        Long userId,
        Long questionId,
        LocalDate date,
        String answerText,
        LocalDateTime createdAt
) {

    public static Reflection create(
            Long userId,
            Long questionId,
            LocalDate date,
            String answerText
    ) {
        return new Reflection(
                null,
                userId,
                questionId,
                date,
                answerText,
                null
        );
    }
}
