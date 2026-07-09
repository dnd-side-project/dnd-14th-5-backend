package com.dnd5.timoapi.domain.customization.presentation.response;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;

public record UnlockedCustomizationItemResponse(
        Long id,
        String name,
        CustomizationItemType type,
        String description,
        String image
) {
    public static UnlockedCustomizationItemResponse from(CustomizationItem model) {
        return new UnlockedCustomizationItemResponse(
                model.id(), model.name(), model.type(), model.description(), model.image());
    }
}
