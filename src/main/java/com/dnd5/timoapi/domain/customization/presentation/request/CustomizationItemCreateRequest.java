package com.dnd5.timoapi.domain.customization.presentation.request;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CustomizationItemCreateRequest(
        @NotBlank
        String name,
        @NotNull
        CustomizationItemType type,
        String description,
        @NotNull
        CustomizationUnlockConditionType unlockConditionType,
        @NotNull
        @Positive
        Integer unlockConditionCount,
        String image
) {
    public CustomizationItem toModel() {
        return CustomizationItem.create(name, type, description, unlockConditionType, unlockConditionCount, image);
    }
}
