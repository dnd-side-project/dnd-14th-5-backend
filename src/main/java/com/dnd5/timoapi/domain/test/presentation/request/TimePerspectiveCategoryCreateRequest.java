package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;
import jakarta.validation.constraints.NotBlank;

public record TimePerspectiveCategoryCreateRequest(
        @NotBlank
        String name,
        String englishName,
        String characterName,
        String personality,
        @NotBlank
        String description,
        Double idealValue
) {
    public TimePerspectiveCategory toModel() {
        return TimePerspectiveCategory.create(name, englishName, characterName, personality, description, idealValue);
    }
}
