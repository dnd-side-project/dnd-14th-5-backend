package com.dnd5.timoapi.domain.customization.domain.model;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

import java.time.LocalDateTime;

public record CustomizationItemImage(
        Long id,
        Long customizationItemId,
        ZtpiCategory category,
        String image,
        String imageWithoutBackground,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static CustomizationItemImage create(
            Long customizationItemId, ZtpiCategory category, String image, String imageWithoutBackground) {
        return new CustomizationItemImage(
                null, customizationItemId, category, image, imageWithoutBackground, null, null, null);
    }
}
