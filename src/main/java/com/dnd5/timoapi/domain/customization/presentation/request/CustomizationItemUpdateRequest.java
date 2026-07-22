package com.dnd5.timoapi.domain.customization.presentation.request;

import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CustomizationItemUpdateRequest(
        String name,
        CustomizationItemType type,
        String description,
        CustomizationUnlockConditionType unlockConditionType,
        @Positive
        Integer unlockConditionCount,
        @Valid
        List<CustomizationItemImageCreateRequest> images
) {
}
