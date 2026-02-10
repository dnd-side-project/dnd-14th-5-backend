package com.dnd5.timoapi.domain.introduction.presentation.response;

import com.dnd5.timoapi.domain.introduction.domain.model.Introduction;

import java.time.LocalDateTime;

public record IntroductionResponse(
        Long id,
        int version,
        String content,
        LocalDateTime createdAt
) {
    public static IntroductionResponse from(Introduction model) {
        return new IntroductionResponse(
                model.id(),
                model.version(),
                model.content(),
                model.createdAt()
        );
    }
}
