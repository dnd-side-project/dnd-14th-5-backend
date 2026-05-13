package com.dnd5.timoapi.domain.group.presentation.request;

import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GroupCreateRequest(
        @NotBlank String name,
        @NotNull GroupType type,
        String image,
        ZtpiCategory category
) {
}
