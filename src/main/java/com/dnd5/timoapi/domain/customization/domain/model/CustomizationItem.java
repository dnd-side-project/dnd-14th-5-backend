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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static CustomizationItem create(
            String name,
            CustomizationItemType type,
            String description,
            CustomizationUnlockConditionType unlockConditionType,
            Integer unlockConditionCount
    ) {
        return new CustomizationItem(null, name, type, description, unlockConditionType, unlockConditionCount, null, null, null);
    }
}
