package com.dnd5.timoapi.domain.customization.presentation.response;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;

public record CustomizationItemResponse(
        Long id,
        String name,
        CustomizationItemType type,
        boolean isUnlocked,
        boolean isEquipped,
        String image,
        String imageWithoutBackground
) {
    public static CustomizationItemResponse from(
            CustomizationItem model, boolean isUnlocked, boolean isEquipped, String image, String imageWithoutBackground) {
        return new CustomizationItemResponse(
                model.id(), model.name(), model.type(), isUnlocked, isEquipped, image, imageWithoutBackground);
    }
}
