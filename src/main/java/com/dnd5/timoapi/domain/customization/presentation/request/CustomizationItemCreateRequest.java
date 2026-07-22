package com.dnd5.timoapi.domain.customization.presentation.request;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

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
        @NotNull
        Boolean usesCharacterImage,
        @NotEmpty
        @Valid
        List<CustomizationItemImageCreateRequest> images
) {
    public CustomizationItem toModel() {
        return CustomizationItem.create(name, type, description, unlockConditionType, unlockConditionCount, usesCharacterImage);
    }
}
