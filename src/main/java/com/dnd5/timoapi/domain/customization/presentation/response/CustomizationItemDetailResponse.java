package com.dnd5.timoapi.domain.customization.presentation.response;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;

public record CustomizationItemDetailResponse(
        Long id,
        String name,
        CustomizationItemType type,
        String description,
        CustomizationUnlockConditionType unlockConditionType,
        Integer unlockConditionCount,
        String image,
        String imageWithoutBackground
) {
    public static CustomizationItemDetailResponse from(CustomizationItem model, String image, String imageWithoutBackground) {
        return new CustomizationItemDetailResponse(
                model.id(),
                model.name(),
                model.type(),
                model.description(),
                model.unlockConditionType(),
                model.unlockConditionCount(),
                image,
                imageWithoutBackground
        );
    }
}
