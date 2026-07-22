package com.dnd5.timoapi.domain.customization.presentation.response;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;

public record EquippedCustomizationResponse(
        Long id,
        String name,
        CustomizationItemType type,
        String image,
        String imageWithoutBackground
) {
    public static EquippedCustomizationResponse from(CustomizationItem model, String image, String imageWithoutBackground) {
        return new EquippedCustomizationResponse(model.id(), model.name(), model.type(), image, imageWithoutBackground);
    }
}
