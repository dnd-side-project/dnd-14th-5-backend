package com.dnd5.timoapi.domain.customization.domain.model;

import java.time.LocalDateTime;

public record CustomizationUserItem(
        Long id,
        Long userId,
        Long customizationItemId,
        boolean isUnlocked,
        boolean isEquipped,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static CustomizationUserItem create(Long userId, Long customizationItemId) {
        return new CustomizationUserItem(null, userId, customizationItemId, false, false, null, null, null);
    }
}
