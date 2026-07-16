package com.dnd5.timoapi.domain.customization.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomizationItemImageRequest(
        @NotNull
        ZtpiCategory category,
        @NotBlank
        String image,
        @NotBlank
        String imageWithoutBackground
) {
}
