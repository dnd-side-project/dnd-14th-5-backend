package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.Test;

import java.time.LocalDateTime;

public record TestDetailResponse(
        Long id,
        String type,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static TestDetailResponse from(Test model) {
        return new TestDetailResponse(
                model.id(),
                model.type(),
                model.name(),
                model.description(),
                model.createdAt()
        );
    }
}
