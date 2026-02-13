package com.dnd5.timoapi.domain.test.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record TimePerspectiveCategoryCreateRequest(
        @NotBlank
        String name,
        @NotBlank
        String character,
        String personality,
        @NotEmpty
        String description
) {
}
