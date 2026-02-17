package com.dnd5.timoapi.domain.test.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record TimePerspectiveCategoryUpdateRequest(
        @NotBlank
        String name,
        String characterName,
        String personality,
        @NotEmpty
        String description
) {
}
