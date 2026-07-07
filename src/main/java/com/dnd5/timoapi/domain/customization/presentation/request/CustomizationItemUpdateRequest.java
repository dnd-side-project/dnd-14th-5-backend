package com.dnd5.timoapi.domain.customization.presentation.request;

import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;
import jakarta.validation.constraints.Positive;

public record CustomizationItemUpdateRequest(
        String name,
        CustomizationItemType type,
        String description,
        CustomizationUnlockConditionType unlockConditionType,
        @Positive
        Integer unlockConditionCount,
        String image
) {
}
