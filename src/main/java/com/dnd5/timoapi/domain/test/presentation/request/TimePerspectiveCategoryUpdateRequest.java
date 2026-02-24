package com.dnd5.timoapi.domain.test.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record TimePerspectiveCategoryUpdateRequest(
        @NotBlank
        String name,
        String englishName,
        String characterName,
        String personality,
        @NotBlank
        String description,
        Double idealValue
) {
}
