package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;

public record TimePerspectiveCategoryDetailResponse(
        Long id,
        String name,
        String characterName,
        String personality,
        String description
) {
    public static TimePerspectiveCategoryDetailResponse from(TimePerspectiveCategory model) {
        return new TimePerspectiveCategoryDetailResponse(
                model.id(),
                model.name(),
                model.characterName(),
                model.personality(),
                model.description()
        );
    }
}
