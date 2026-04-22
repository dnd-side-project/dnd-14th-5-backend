package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;
import java.time.LocalDateTime;

public record TimePerspectiveCategoryDetailResponse(
        Long id,
        String name,
        String englishName,
        String characterName,
        String personality,
        String description,
        Double idealScore,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TimePerspectiveCategoryDetailResponse from(TimePerspectiveCategory model) {
        return new TimePerspectiveCategoryDetailResponse(
                model.id(),
                model.name(),
                model.englishName(),
                model.characterName(),
                model.personality(),
                model.description(),
                model.idealScore(),
                model.createdAt(),
                model.updatedAt()
        );
    }
}
