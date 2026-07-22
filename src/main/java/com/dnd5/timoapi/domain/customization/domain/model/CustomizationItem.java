package com.dnd5.timoapi.domain.customization.domain.model;

import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;

import java.time.LocalDateTime;

public record CustomizationItem(
        Long id,
        String name,
        CustomizationItemType type,
        String description,
        CustomizationUnlockConditionType unlockConditionType,
        Integer unlockConditionCount,
        boolean usesCharacterImage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static CustomizationItem create(
            String name,
            CustomizationItemType type,
            String description,
            CustomizationUnlockConditionType unlockConditionType,
            Integer unlockConditionCount,
            boolean usesCharacterImage
    ) {
        return new CustomizationItem(null, name, type, description, unlockConditionType, unlockConditionCount, usesCharacterImage, null, null, null);
    }
}
