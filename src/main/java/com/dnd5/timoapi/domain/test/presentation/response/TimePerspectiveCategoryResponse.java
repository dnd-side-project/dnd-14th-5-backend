package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;
import java.time.LocalDateTime;

public record TimePerspectiveCategoryResponse(
        Long id,
        String name,
        String characterName,
        String personality,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TimePerspectiveCategoryResponse from(TimePerspectiveCategory model) {
        return new TimePerspectiveCategoryResponse(
                model.id(),
                model.name(),
                model.characterName(),
                model.personality(),
                model.description(),
                model.createdAt(),
                model.updatedAt()
        );
    }
}
